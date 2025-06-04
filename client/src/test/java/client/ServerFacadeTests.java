package client;

import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;
import ui.ServerFacade.AuthResult;
import ui.ServerFacade.GameResult;
import ui.ServerFacade.ListGamesResult;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setup() throws Exception {
        facade.clear();
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    void registerValidUser() throws Exception {
        AuthResult authData = facade.register("testUser", "password123", "test@email.com");
        Assertions.assertNotNull(authData);
        Assertions.assertNotNull(authData.authToken);
        Assertions.assertNotNull(authData.username);
        Assertions.assertEquals("testUser", authData.username);
        Assertions.assertTrue(authData.authToken.length() > 10);
    }

    @Test
    void registerDuplicateUser() throws Exception {
        facade.register("duplicateUser", "password", "test@email.com");

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.register("duplicateUser", "differentPassword", "different@email.com");
        });
        Assertions.assertTrue(exception.getMessage().contains("HTTP Error: 403"));
    }

    @Test
    void registerInvalidUser() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            facade.register("", "password", "test@email.com");
        });
        Assertions.assertTrue(exception.getMessage().contains("Invalid registration data"));

        IllegalArgumentException exception2 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            facade.register("testUser", null, "test@email.com");
        });
        Assertions.assertTrue(exception2.getMessage().contains("Invalid registration data"));
    }

    @Test
    void loginValidUser() throws Exception {
        facade.register("loginUser", "myPassword", "login@email.com");

        AuthResult authData = facade.login("loginUser", "myPassword");
        Assertions.assertNotNull(authData);
        Assertions.assertNotNull(authData.authToken);
        Assertions.assertEquals("loginUser", authData.username);
    }

    @Test
    void loginInvalidPassword() throws Exception {
        facade.register("loginUser", "correctPassword", "login@email.com");

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.login("loginUser", "wrongPassword");
        });
        Assertions.assertTrue(exception.getMessage().contains("HTTP Error: 401"));
    }

    @Test
    void loginNonexistentUser() {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.login("nonexistentUser", "password123");
        });
        Assertions.assertTrue(exception.getMessage().contains("HTTP Error: 401"));
    }
    
    @Test
    void logoutValidUser() throws Exception {
        AuthResult authData = facade.register("logoutUser", "password123", "logout@email.com");

        Assertions.assertDoesNotThrow(() -> {
            facade.logout(authData.authToken);
        });
    }

    @Test
    void logoutInvalidToken() {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.logout("invalidToken123");
        });
        Assertions.assertTrue(exception.getMessage().contains("HTTP Error: 401"));
    }

    @Test
    void logoutNullToken() {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.logout(null);
        });
        Assertions.assertTrue(exception.getMessage().contains("HTTP Error: 401"));
    }

    @Test
    void createGameValid() throws Exception {
        AuthResult authData = facade.register("gameCreator", "password123", "creator@email.com");

        GameResult gameResult = facade.createGame("TestGame", authData.authToken);
        Assertions.assertNotNull(gameResult);
        Assertions.assertTrue(gameResult.gameID > 0);
    }

    @Test
    void createGameInvalidAuth() {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.createGame("TestGame", "invalidToken");
        });
        Assertions.assertTrue(exception.getMessage().contains("HTTP Error: 401"));
    }

    @Test
    void createGameNullName() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            facade.createGame(null, "validAuthToken");
        });
        Assertions.assertTrue(exception.getMessage().contains("Game name cannot be null or empty"));
    }

    @Test
    void listGamesEmpty() throws Exception {
        AuthResult authData = facade.register("listUser", "password123", "list@email.com");

        ListGamesResult result = facade.listGames(authData.authToken);
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.games);
        Assertions.assertEquals(0, result.games.length);
    }

    @Test
    void listGamesWithGames() throws Exception {
        AuthResult authData = facade.register("listUser", "password123", "list@email.com");

        facade.createGame("Game1", authData.authToken);
        facade.createGame("Game2", authData.authToken);

        ListGamesResult result = facade.listGames(authData.authToken);
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.games);
        Assertions.assertEquals(2, result.games.length);
    }

    @Test
    void listGamesInvalidAuth() {
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.listGames("invalidToken");
        });
        Assertions.assertTrue(exception.getMessage().contains("HTTP Error: 401"));
    }

    @Test
    void joinGameValid() throws Exception {
        AuthResult authData = facade.register("joinUser", "password123", "join@email.com");
        GameResult gameResult = facade.createGame("JoinTestGame", authData.authToken);

        Assertions.assertDoesNotThrow(() -> {
            facade.joinGame(gameResult.gameID, "WHITE", authData.authToken);
        });
    }

    @Test
    void joinGameInvalidAuth() throws Exception {
        AuthResult authData = facade.register("joinUser", "password123", "join@email.com");
        GameResult gameResult = facade.createGame("JoinTestGame", authData.authToken);

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.joinGame(gameResult.gameID, "WHITE", "invalidToken");
        });
        Assertions.assertTrue(exception.getMessage().contains("HTTP Error: 401"));
    }

    @Test
    void joinGameInvalidGameID() throws Exception {
        AuthResult authData = facade.register("joinUser", "password123", "join@email.com");

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.joinGame(999999, "WHITE", authData.authToken);
        });
        Assertions.assertTrue(exception.getMessage().contains("HTTP Error"));
    }

    @Test
    void joinGameColorAlreadyTaken() throws Exception {
        AuthResult authData1 = facade.register("joinUser1", "password123", "join1@email.com");
        AuthResult authData2 = facade.register("joinUser2", "password123", "join2@email.com");
        GameResult gameResult = facade.createGame("JoinTestGame", authData1.authToken);

        facade.joinGame(gameResult.gameID, "WHITE", authData1.authToken);

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.joinGame(gameResult.gameID, "WHITE", authData2.authToken);
        });
        Assertions.assertTrue(exception.getMessage().contains("HTTP Error: 403"));
    }

    @Test
    void joinGameInvalidColor() throws Exception {
        AuthResult authData = facade.register("joinUser", "password123", "join@email.com");
        GameResult gameResult = facade.createGame("JoinTestGame", authData.authToken);

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            facade.joinGame(gameResult.gameID, "PURPLE", authData.authToken);
        });
        Assertions.assertTrue(exception.getMessage().contains("HTTP Error"));
    }
}