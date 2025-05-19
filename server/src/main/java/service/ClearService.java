package service;

import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;

public class ClearService {
    private final UserDAO userDAO;
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public ClearService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void clear() throws DataAccessException {
        userDAO.clearUser();
        gameDAO.clearGame();
        authDAO.clearAuth();
    }
}
