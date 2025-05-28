package dataaccess;

import model.GameData;

import java.util.List;

public interface GameDAO {
    List<GameData> getAllGames() throws DataAccessException, DatabaseServiceException;

    GameData getGame(int gameId) throws DataAccessException, DatabaseServiceException;

    void createGame(GameData gameData) throws DataAccessException, DatabaseServiceException;

    void updateGame(GameData gameData) throws DataAccessException, DatabaseServiceException;


    void clearGame() throws DataAccessException, DatabaseServiceException;
}
