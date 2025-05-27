package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLGameDAO implements GameDAO {
    private final Gson gson = new Gson();

    public SQLGameDAO() throws DataAccessException {
        createGameTable();
    }

    private void createGameTable() throws DataAccessException {
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
            throw new DataAccessException("Error creating games table", e);
        }
    }

    @Override
    public List<GameData> getAllGames() throws DataAccessException {
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
            throw new DataAccessException("Error retrieving all games", e);
        }
    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        String sql = "SELECT game_id, white_username, black_username, game_name, game_state FROM games WHERE game_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractGameDataFromResultSet(rs);
                }
                throw new DataAccessException("Game not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game", e);
        }
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException {
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

            // Serialize the ChessGame object to JSON
            String gameStateJson = gson.toJson(gameData.game());
            stmt.setString(5, gameStateJson);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game", e);
        }
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
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
                throw new DataAccessException("Game not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game", e);
        }
    }

    @Override
    public void clearGame() throws DataAccessException {
        String sql = "DELETE FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games", e);
        }
    }

    private boolean gameExists(int gameId) throws DataAccessException {
        String sql = "SELECT 1 FROM games WHERE game_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if game exists", e);
        }
    }

    private GameData extractGameDataFromResultSet(ResultSet rs) throws SQLException, DataAccessException {
        int gameId = rs.getInt("game_id");
        String whiteUsername = rs.getString("white_username");
        String blackUsername = rs.getString("black_username");
        String gameName = rs.getString("game_name");
        String gameStateJson = rs.getString("game_state");

        try {
            ChessGame game = gson.fromJson(gameStateJson, ChessGame.class);
            return new GameData(gameId, whiteUsername, blackUsername, gameName, game);
        } catch (Exception e) {
            throw new DataAccessException("Error deserializing game state", e);
        }
    }
}