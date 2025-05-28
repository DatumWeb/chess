package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameListServiceTest {
    private GameListService gameListService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private String validAuthToken;
    private String invalidAuthToken;

    @BeforeEach
    void setUp() throws DataAccessException, DatabaseServiceException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameListService = new GameListService(gameDAO, authDAO);

        AuthData authData = new AuthData("validToken", "testUser");
        authDAO.createAuthToken(authData);
        validAuthToken = authData.authToken();
        invalidAuthToken = "invalidToken";

        gameDAO.createGame(new GameData(101, "whiteUser1", "blackUser1", "Game 1", null));
        gameDAO.createGame(new GameData(102, "whiteUser2", null, "Game 2", null));
        gameDAO.createGame(new GameData(103, null, "blackUser3", "Game 3", null));
    }

    @Test
    @DisplayName("Successfully get list of games")
    void testGetGameListSuccess() throws DataAccessException, DatabaseServiceException {
        List<GameData> games = gameListService.getGameList(validAuthToken);

        assertNotNull(games, "Games list should not be null");
        assertEquals(3, games.size(), "Should return all 3 games");

        boolean foundGame1 = false;
        boolean foundGame2 = false;
        boolean foundGame3 = false;

        for (GameData game : games) {
            if (game.gameID() == 101 && "Game 1".equals(game.gameName())) {
                foundGame1 = true;
            } else if (game.gameID() == 102 && "Game 2".equals(game.gameName())) {
                foundGame2 = true;
            } else if (game.gameID() == 103 && "Game 3".equals(game.gameName())) {
                foundGame3 = true;
            }
        }

        assertTrue(foundGame1, "Game 1 should be in the returned list");
        assertTrue(foundGame2, "Game 2 should be in the returned list");
        assertTrue(foundGame3, "Game 3 should be in the returned list");
    }

    @Test
    @DisplayName("Fail to get games with invalid auth token")
    void testGetGameListUnauthorized() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                gameListService.getGameList(invalidAuthToken)
        );

        assertEquals("Error: unauthorized", exception.getMessage(), "Error message should indicate unauthorized access");
    }
}