package dataaccess;

import model.GameData;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> gameDataMap = new HashMap<>();

    @Override
    public List<GameData> getAllGames() throws DataAccessException {
        return new ArrayList<>(gameDataMap.values());
    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        GameData gameData = gameDataMap.get(gameId);
        if (gameData == null) {
            throw new DataAccessException("Game not found");
        }
        return gameData;
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException {
        if (gameDataMap.containsKey(gameData.gameID())) {
            throw new DataAccessException("Game already exists");
        }
        gameDataMap.put(gameData.gameID(), gameData);

    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        if (!gameDataMap.containsKey(gameData.gameID())) {
            throw new DataAccessException("Game not found");
        }
        gameDataMap.put(gameData.gameID(), gameData);
    }

    @Override
    public void clearGame() {
        gameDataMap.clear();
    }
}
