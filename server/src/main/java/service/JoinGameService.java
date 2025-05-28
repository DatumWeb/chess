package service;

import dataaccess.DatabaseServiceException;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.GameData;
import model.AuthData;

public class JoinGameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public JoinGameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException, DatabaseServiceException {
        if (gameID <= 0) {
            throw new DataAccessException("Error: bad request");
        }
        AuthData authData = authDAO.getAuthToken(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        String username = authData.username();

        if (playerColor == null || (!playerColor.equals("WHITE") && !playerColor.equals("BLACK"))) {
            throw new DataAccessException("Error: bad request");
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (playerColor.equals("WHITE") && gameData.whiteUsername() != null) {
            throw new DataAccessException("Error: already taken");
        } else if (playerColor.equals("BLACK") && gameData.blackUsername() != null) {
            throw new DataAccessException("Error: already taken");
        }

        if (playerColor.equals("WHITE")) {
            gameData = new GameData(gameData.gameID(), username, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else if (playerColor.equals("BLACK")) {
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), username, gameData.gameName(), gameData.game());
        }

        gameDAO.updateGame(gameData);
    }
}