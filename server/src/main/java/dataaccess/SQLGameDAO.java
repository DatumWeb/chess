package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLGameDAO implements GameDAO {
    private final Gson gson = new Gson();

    public SQLGameDAO() throws DataAccessException, DatabaseServiceException { // Updated signature
        createGameTable();
    }
    private boolean isConnectionIssue(SQLException e) {
        String sqlState = e.getSQLState();
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

        if (sqlState != null && sqlState.startsWith("08")) {
            return true;
        }
        return message.contains("communications link failure") ||
                message.contains("connection refused") ||
                message.contains("connection timed out") ||
                message.contains("cannot create poolableconnectionfactory") ||
                message.contains("unknown host") ||
                message.contains("network is unreachable");
    }

    private void createGameTable() throws DataAccessException, DatabaseServiceException {
        String sql = """
                CREATE TABLE IF NOT EXISTS games (
                    game_id INT NOT NULL PRIMARY KEY,
                    white_username VARCHAR(255) NULL,
                    black_username VARCHAR(255) NULL,
                    game_name VARCHAR(255) NOT NULL,
                    game_state TEXT NOT NULL
                )
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Failed to connect to database for game table creation.", e);
            }
            throw new DataAccessException("Error creating games table", e);
        }
    }

    @Override
    public List<GameData> getAllGames() throws DataAccessException, DatabaseServiceException {
        String sql = "SELECT game_id, white_username, black_username, game_name, game_state FROM games";
        List<GameData> games = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                GameData gameData = extractGameDataFromResultSet(rs);
                games.add(gameData);
            }
            return games;
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while retrieving all games.", e);
            }
            throw new DataAccessException("Error retrieving all games", e);
        }
    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException, DatabaseServiceException { // Updated signature
        String sql = "SELECT game_id, white_username, black_username, game_name, game_state FROM games WHERE game_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractGameDataFromResultSet(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while retrieving game.", e);
            }
            throw new DataAccessException("Error retrieving game", e);
        }
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException, DatabaseServiceException { // Updated signature
        if (gameExists(gameData.gameID())) {
            throw new DataAccessException("Game already exists");
        }

        String sql = "INSERT INTO games (game_id, white_username, black_username, game_name, game_state) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameData.gameID());
            stmt.setString(2, gameData.whiteUsername());
            stmt.setString(3, gameData.blackUsername());
            stmt.setString(4, gameData.gameName());

            String gameStateJson = gson.toJson(gameData.game());
            stmt.setString(5, gameStateJson);

            stmt.executeUpdate();
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while creating game.", e);
            }
            if (e.getSQLState() != null && (e.getSQLState().equals("23000") || e.getSQLState().equals("23505")) || (e.getErrorCode() == 1062 || e.getErrorCode() == 19)) {
                throw new DataAccessException("Game already exists", e); // More specific for duplicate
            }
            throw new DataAccessException("Error creating game", e);
        } catch (JsonSyntaxException e) {
            throw new DatabaseServiceException("Error serializing game state for database.", e); // Treat as internal error
        }
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException, DatabaseServiceException { // Updated signature
        String sql = "UPDATE games SET white_username = ?, black_username = ?, game_name = ?, game_state = ? WHERE game_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, gameData.whiteUsername());
            stmt.setString(2, gameData.blackUsername());
            stmt.setString(3, gameData.gameName());

            String gameStateJson = gson.toJson(gameData.game());
            stmt.setString(4, gameStateJson);

            stmt.setInt(5, gameData.gameID());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Game not found for update"); // Specific data access condition
            }
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while updating game.", e);
            }
            throw new DataAccessException("Error updating game", e);
        } catch (JsonSyntaxException e) {
            throw new DatabaseServiceException("Error serializing game state for database update.", e); // Treat as internal error
        }
    }

    @Override
    public void clearGame() throws DataAccessException, DatabaseServiceException {
        String sql = "DELETE FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while clearing games.", e);
            }
            throw new DataAccessException("Error clearing games", e);
        }
    }

    private boolean gameExists(int gameId) throws DataAccessException, DatabaseServiceException { // Updated signature
        String sql = "SELECT 1 FROM games WHERE game_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while checking if game exists.", e);
            }
            throw new DataAccessException("Error checking if game exists", e);
        }
    }

    private GameData extractGameDataFromResultSet(ResultSet rs) throws SQLException, DataAccessException, DatabaseServiceException {
        // SQLException is declared to be caught by the caller
        int gameId = rs.getInt("game_id");
        String whiteUsername = rs.getString("white_username");
        String blackUsername = rs.getString("black_username");
        String gameName = rs.getString("game_name");
        String gameStateJson = rs.getString("game_state");

        try {
            ChessGame game = gson.fromJson(gameStateJson, ChessGame.class);
            if (game == null && gameStateJson != null && !gameStateJson.equalsIgnoreCase("null")) {
                // This indicates a potentially corrupted or unexpected JSON that GSON couldn't parse to ChessGame
                throw new DatabaseServiceException("Failed to deserialize game state from database: JSON parsed to null for game ID " + gameId);
            }
            return new GameData(gameId, whiteUsername, blackUsername, gameName, game);
        } catch (JsonSyntaxException e) {
            // This is a critical error if data in DB is corrupt
            throw new DatabaseServiceException("Error deserializing game state from database for game ID " + gameId, e);
        }
    }
}