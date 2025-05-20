package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class JoinGameServiceTest {
    private JoinGameService joinGameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private String validAuthToken;
    private String invalidAuthToken;
    private final int validGameId = 101;
    private final int invalidGameId = 999;
    private final String username = "testUser";

    @BeforeEach
    void setUp() throws DataAccessException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        joinGameService = new JoinGameService(gameDAO, authDAO);

        AuthData authData = new AuthData("validToken", username);
        authDAO.createAuthToken(authData);
        validAuthToken = authData.authToken();
        invalidAuthToken = "invalidToken";

        gameDAO.createGame(new GameData(validGameId, null, null, "Open Game", null));
        gameDAO.createGame(new GameData(102, "existingWhiteUser", null, "White Taken", null));
        gameDAO.createGame(new GameData(103, null, "existingBlackUser", "Black Taken", null));
    }

    @Test
    @DisplayName("Successfully join game as WHITE")
    void testJoinGameAsWhiteSuccess() throws DataAccessException {
        joinGameService.joinGame(validAuthToken, validGameId, "WHITE");

        GameData game = gameDAO.getGame(validGameId);
        assertNotNull(game, "Game should exist");
        assertEquals(username, game.whiteUsername(), "White username should be set to test user");
        assertNull(game.blackUsername(), "Black username should still be null");
    }

    @Test
    @DisplayName("Successfully join game as BLACK")
    void testJoinGameAsBlackSuccess() throws DataAccessException {
        joinGameService.joinGame(validAuthToken, validGameId, "BLACK");

        GameData game = gameDAO.getGame(validGameId);
        assertNotNull(game, "Game should exist");
        assertEquals(username, game.blackUsername(), "Black username should be set to test user");
        assertNull(game.whiteUsername(), "White username should still be null");
    }

    @Test
    @DisplayName("Fail to join non-existent game")
    void testJoinNonExistentGame() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                joinGameService.joinGame(validAuthToken, invalidGameId, "WHITE")
        );

        assertEquals("Game not found", exception.getMessage(), "Error message should indicate bad request");
    }

    @Test
    @DisplayName("Fail to join game with invalid auth token")
    void testJoinGameUnauthorized() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                joinGameService.joinGame(invalidAuthToken, validGameId, "WHITE")
        );

        assertEquals("Error: unauthorized", exception.getMessage(), "Error message should indicate unauthorized access");
    }

    @Test
    @DisplayName("Fail to join game with invalid color")
    void testJoinGameInvalidColor() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                joinGameService.joinGame(validAuthToken, validGameId, "INVALID")
        );

        assertEquals("Error: bad request", exception.getMessage(), "Error message should indicate bad request");
    }

    @Test
    @DisplayName("Fail to join game with already taken position")
    void testJoinGamePositionTaken() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                joinGameService.joinGame(validAuthToken, 102, "WHITE")
        );

        assertEquals("Error: already taken", exception.getMessage(), "Error message should indicate position is already taken");
    }
}