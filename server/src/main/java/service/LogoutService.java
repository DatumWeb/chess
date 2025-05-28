package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseServiceException;

public class LogoutService {
    private final AuthDAO authDAO;

    public LogoutService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public boolean logout(String authToken) throws DataAccessException, DatabaseServiceException {
        if (authToken == null) {
            throw new DataAccessException("Auth token is missing.");
        }

        try {
            authDAO.deleteAuthToken(authToken);
        } catch (DatabaseServiceException e) {
            throw new DatabaseServiceException("Database failure during logout.");
        } catch (DataAccessException exception) {
            if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("failed to get connection")) {
                throw new DatabaseServiceException("Database failure during logout.");
            }
            throw new DataAccessException("Error: unauthorized");
        }

        return true;
    }
}
