package service;

import dataaccess.DatabaseServiceException;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.GameData;
import model.AuthData;

import java.util.List;

public class GameListService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameListService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public List<GameData> getGameList(String authToken) throws DataAccessException, DatabaseServiceException {
        AuthData authData = authDAO.getAuthToken(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        return gameDAO.getAllGames();
    }
}
