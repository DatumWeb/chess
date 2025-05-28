package service;

import chess.ChessBoard;
import dataaccess.DatabaseServiceException;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.GameData;
import model.AuthData;
import chess.ChessGame;

import java.util.UUID;

public class GameCreateService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameCreateService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public GameData createGame(String authToken, String gameName) throws DataAccessException, DatabaseServiceException {
        AuthData authData = authDAO.getAuthToken(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        String whiteUsername = null;
        String blackUsername = null;

        ChessGame game = new ChessGame();
        ChessBoard gameBoard = new ChessBoard();
        gameBoard.resetBoard();

        GameData newGame = new GameData(generateGameId(), whiteUsername, blackUsername, gameName, game);

        gameDAO.createGame(newGame);

        return newGame;
    }

    private int generateGameId() {
        return Math.abs(UUID.randomUUID().hashCode());
    }
}
