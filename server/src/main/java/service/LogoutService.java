package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseServiceException;

public class LogoutService {
    private final AuthDAO authDAO;

    public LogoutService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public boolean logout(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("Auth token is missing.");
        }

        try {
            authDAO.deleteAuthToken(authToken);
        } catch (DataAccessException exception) {
            throw new DataAccessException("Error: unauthorized");
        } catch (DatabaseServiceException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
