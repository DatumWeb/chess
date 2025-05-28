package dataaccess;

import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLGameDAOTest {
    private SQLGameDAO gameDAO;

    @BeforeEach
    void setUp() throws DataAccessException, DatabaseServiceException {
        gameDAO = new SQLGameDAO();
        gameDAO.clearGame();
    }

    @Test
    @DisplayName("Successfully create and retrieve a game")
    void testCreateAndRetrieveGame() throws DataAccessException, DatabaseServiceException {
        GameData game = new GameData(100, "WhitePlayer", "BlackPlayer", "TestGame", new ChessGame());
        gameDAO.createGame(game);

        GameData retrievedGame = gameDAO.getGame(100);

        assertNotNull(retrievedGame, "Game should be retrievable after creation");
        assertEquals("TestGame", retrievedGame.gameName(), "Game name should match");
        assertNotNull(retrievedGame.game(), "Game state should be retrievable");
    }

    @Test
    @DisplayName("Fail to retrieve non-existent game")
    void testGetNonExistentGame() {
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(9999), "Retrieving a non-existent game should throw an exception");
    }

    @Test
    @DisplayName("Successfully retrieve all games")
    void testGetAllGames() throws DataAccessException, DatabaseServiceException {
        gameDAO.createGame(new GameData(101, "White1", "Black1", "Game1", new ChessGame()));
        gameDAO.createGame(new GameData(102, "White2", "Black2", "Game2", new ChessGame()));

        List<GameData> games = gameDAO.getAllGames();

        assertEquals(2, games.size(), "Should retrieve all created games");
    }

    @Test
    @DisplayName("Fail to retrieve games when none exist")
    void testGetAllGamesEmpty() throws DataAccessException, DatabaseServiceException {
        List<GameData> games = gameDAO.getAllGames();
        assertTrue(games.isEmpty(), "Retrieving all games from an empty database should return an empty list");
    }

    @Test
    @DisplayName("Successfully update a game")
    void testUpdateGame() throws DataAccessException, DatabaseServiceException {
        GameData game = new GameData(200, "WhitePlayer", "BlackPlayer", "InitialGame", new ChessGame());
        gameDAO.createGame(game);

        GameData updatedGame = new GameData(200, "UpdatedWhite", "UpdatedBlack", "UpdatedGame", new ChessGame());
        gameDAO.updateGame(updatedGame);

        GameData retrievedGame = gameDAO.getGame(200);

        assertEquals("UpdatedGame", retrievedGame.gameName(), "Game name should be updated");
        assertEquals("UpdatedWhite", retrievedGame.whiteUsername(), "White player should be updated");
        assertEquals("UpdatedBlack", retrievedGame.blackUsername(), "Black player should be updated");
    }

    @Test
    @DisplayName("Fail to update a non-existent game")
    void testUpdateNonExistentGame() {
        GameData game = new GameData(999, "WhitePlayer", "BlackPlayer", "NonExistentGame", new ChessGame());

        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameDAO.updateGame(game));

        assertEquals("Game not found for update", exception.getMessage(), "Updating a non-existent game should throw an exception");
    }

    @Test
    @DisplayName("Successfully clear all games")
    void testClearGame() throws DataAccessException, DatabaseServiceException {
        gameDAO.createGame(new GameData(301, "White1", "Black1", "Game1", new ChessGame()));
        gameDAO.createGame(new GameData(302, "White2", "Black2", "Game2", new ChessGame()));

        gameDAO.clearGame();

        List<GameData> games = gameDAO.getAllGames();
        assertTrue(games.isEmpty(), "After clearing, retrieving all games should return an empty list");
    }


    @Test
    @DisplayName("Successfully clear an empty game table")
    void testClearEmptyGameTable() throws DataAccessException, DatabaseServiceException {
        gameDAO.clearGame();

        List<GameData> games = gameDAO.getAllGames();
        assertTrue(games.isEmpty(), "Clearing an empty game table should still return an empty list");
    }
}