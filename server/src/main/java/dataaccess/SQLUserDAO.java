package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws DataAccessException, DatabaseServiceException {
        createUserTable();
    }

    private void createUserTable() throws DataAccessException, DatabaseServiceException {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) NOT NULL PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL
                )
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (SQLDAOUtils.isConnectionIssue(e)) {
                throw new DatabaseServiceException("Failed to connect to database for table creation.", e);
            }
            throw new DataAccessException("Error creating users table", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException, DatabaseServiceException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            if (SQLDAOUtils.isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while retrieving user.", e);
            }
            throw new DataAccessException("Error retrieving user", e);
        }
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException, DatabaseServiceException {
        if (userExists(userData.username())) {
            throw new DataAccessException("Error: already taken");
        }

        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userData.username());
            stmt.setString(2, userData.password());
            stmt.setString(3, userData.email());

            stmt.executeUpdate();
        } catch (SQLException e) {
            if (SQLDAOUtils.isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while creating user.", e);
            }
            if (e.getSQLState() != null && (e.getSQLState().equals("23000") ||
                    e.getSQLState().equals("23505")) || (e.getErrorCode() == 1062 ||
                    e.getErrorCode() == 19)) {
                throw new DataAccessException("Error: already taken", e);
            }
            throw new DataAccessException("Error creating user", e);
        }
    }

    @Override
    public void clearUser() throws DataAccessException, DatabaseServiceException {
        String sql = "DELETE FROM users";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (SQLDAOUtils.isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while clearing users.", e);
            }
            throw new DataAccessException("Error clearing users", e);
        }
    }

    private boolean userExists(String username) throws DataAccessException, DatabaseServiceException {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            if (SQLDAOUtils.isConnectionIssue(e)) {
                throw new DatabaseServiceException("Database connection error while checking if user exists.", e);
            }
            throw new DataAccessException("Error checking if user exists", e);
        }
    }
}