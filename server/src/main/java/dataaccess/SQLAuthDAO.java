package dataaccess;

import model.AuthData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException, DatabaseServiceException { // Updated signature
        createAuthTokenTable();
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

    private void createAuthTokenTable() throws DataAccessException, DatabaseServiceException { // Updated signature
        String sql = """
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    auth_token VARCHAR(255) NOT NULL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL
                )
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Failed to connect to database for auth table creation.", e);
            }
            throw new DataAccessException("Error creating auth token table", e);
        }
    }

    @Override
    public AuthData getAuthToken(String authToken) throws DataAccessException, DatabaseServiceException { // Updated signature
        String sql = "SELECT auth_token, username FROM auth_tokens WHERE auth_token = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(rs.getString("auth_token"), rs.getString("username"));
                }
                return null;
            }
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while getting auth token.", e);
            }
            throw new DataAccessException("Error getting auth token", e);
        }
    }

    @Override
    public void createAuthToken(AuthData authData) throws DataAccessException, DatabaseServiceException { // Updated signature
        String sql = "INSERT INTO auth_tokens (auth_token, username) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authData.authToken());
            stmt.setString(2, authData.username());

            stmt.executeUpdate();
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while creating auth token.", e);
            }
            if (e.getSQLState() != null && (e.getSQLState().equals("23000") || e.getSQLState().equals("23505")) || (e.getErrorCode() == 1062 || e.getErrorCode() == 19)) {
                throw new DataAccessException("Auth token already exists", e);
            }
            throw new DataAccessException("Error creating auth token", e);
        }
    }

    @Override
    public void deleteAuthToken(String authToken) throws DataAccessException, DatabaseServiceException { // Updated signature
        String sql = "DELETE FROM auth_tokens WHERE auth_token = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Error: unauthorized");
            }
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while deleting auth token.", e);
            }
            throw new DataAccessException("Error deleting auth token", e);
        }
    }

    @Override
    public void clearAuth() throws DataAccessException, DatabaseServiceException {
        String sql = "DELETE FROM auth_tokens";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while clearing auth tokens.", e);
            }
            throw new DataAccessException("Error clearing auth tokens", e);
        }
    }
}