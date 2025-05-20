package dataaccess;

import model.GameData;

import java.util.List;

public interface GameDAO {
    List<GameData> getAllGames() throws DataAccessException;

    GameData getGame(int gameId) throws DataAccessException;

    void createGame(GameData gameData) throws DataAccessException;

    void updateGame(GameData gameData) throws DataAccessException;


    void clearGame();
}
