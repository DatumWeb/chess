package dataaccess;

import model.AuthData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {
        createAuthTokenTable();
    }

    private void createAuthTokenTable() throws DataAccessException {
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
            throw new DataAccessException("Error creating auth token table", e);
        }
    }

    @Override
    public AuthData getAuthToken(String authToken) throws DataAccessException {
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
            throw new DataAccessException("Error getting auth token", e);
        }
    }

    @Override
    public void createAuthToken(AuthData authData) throws DataAccessException {
        String sql = "INSERT INTO auth_tokens (auth_token, username) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authData.authToken());
            stmt.setString(2, authData.username());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth token", e);
        }
    }

    @Override
    public void deleteAuthToken(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth_tokens WHERE auth_token = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Error: Token not found.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth token", e);
        }
    }

    @Override
    public void clearAuth() throws DataAccessException {
        String sql = "DELETE FROM auth_tokens";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing auth tokens", e);
        }
    }
}