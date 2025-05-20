package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ClearServiceTest {
    private MemoryUserDAO userDAO;
    private MemoryGameDAO gameDAO;
    private MemoryAuthDAO authDAO;
    private ClearService clearService;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        clearService = new ClearService(userDAO, gameDAO, authDAO);

        //a base set of single values
        userDAO.createUser(new UserData("MrTester", "123PASSWOrD", "fakeemail@gmail.com"));
        gameDAO.createGame(new GameData(44, "WhiteTester", "BlackTester", "gameNameTest", null));
        System.out.println("Game stored: " + gameDAO.getGame(44));

        authDAO.createAuthToken(new AuthData("testAuthToken", "MrTesterToken"));
    }

    @Test
    void clearOneSet() throws DataAccessException {
        assertNotNull(userDAO.getUser("MrTester"), "User should exist before clearing");
        assertNotNull(gameDAO.getGame(44), "Game should exist before clearing");
        assertNotNull(authDAO.getAuthToken("testAuthToken"), "AuthToken should exist before clearing");

        clearService.clear();

        assertNull(userDAO.getUser("MrTester"));
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(44));
        assertNull(authDAO.getAuthToken("MrTesterToken"));
    }

    @Test
    void clearMultipleSets() throws DataAccessException {
        userDAO.createUser(new UserData("User1", "Pass1", "email1@gmail.com"));
        userDAO.createUser(new UserData("User2", "Pass2", "email2@gmail.com"));
        gameDAO.createGame(new GameData(101, "White1", "Black1", "Game1", null));
        gameDAO.createGame(new GameData(102, "White2", "Black2", "Game2", null));
        authDAO.createAuthToken(new AuthData("AuthToken1", "User1Token"));
        authDAO.createAuthToken(new AuthData("AuthToken2", "User2Token"));

        assertNotNull(userDAO.getUser("User1"), "User1 should exist before clearing");
        assertNotNull(userDAO.getUser("User2"), "User2 should exist before clearing");
        assertNotNull(gameDAO.getGame(101), "Game1 should exist before clearing");
        assertNotNull(gameDAO.getGame(102), "Game2 should exist before clearing");
        assertNotNull(authDAO.getAuthToken("AuthToken1"), "AuthToken1 should exist before clearing");
        assertNotNull(authDAO.getAuthToken("AuthToken2"), "AuthToken2 should exist before clearing");

        clearService.clear();

        assertNull(userDAO.getUser("User1"));
        assertNull(userDAO.getUser("User2"));
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(101));
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(102));
        assertNull(authDAO.getAuthToken("AuthToken1"));
        assertNull(authDAO.getAuthToken("AuthToken2"));
    }


}
