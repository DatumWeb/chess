package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;

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
        }

        return true;
    }
}
