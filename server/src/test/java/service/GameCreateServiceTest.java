package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class GameCreateServiceTest {
    private GameCreateService gameCreateService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private String validAuthToken;
    private String invalidAuthToken;
    private String gameName = "TestGame";

    @BeforeEach
    void setUp() throws DataAccessException, DatabaseServiceException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameCreateService = new GameCreateService(gameDAO, authDAO);

        AuthData authData = new AuthData("validToken", "testUser");
        authDAO.createAuthToken(authData);
        validAuthToken = authData.authToken();
        invalidAuthToken = "invalidToken";
    }

    @Test
    @DisplayName("Successfully create a game")
    void testCreateGameSuccess() throws DataAccessException, DatabaseServiceException {
        GameData createdGame = gameCreateService.createGame(validAuthToken, gameName);

        assertNotNull(createdGame, "Created game should not be null.");
        assertEquals(gameName, createdGame.gameName(), "Game name should match.");
    }

    @Test
    @DisplayName("Fail to create game with invalid auth token")
    void testCreateGameUnauthorized() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                gameCreateService.createGame(invalidAuthToken, gameName)
        );

        assertEquals("Error: unauthorized", exception.getMessage(), "Error message should indicate unauthorized access.");
    }
}
