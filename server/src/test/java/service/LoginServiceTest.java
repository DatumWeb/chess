package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {
    private LoginService loginService;
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private final String username = "testUser";
    private final String password = "testPassword";
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() throws DataAccessException, DatabaseServiceException {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        loginService = new LoginService(userDAO, authDAO);

        userDAO.createUser(new UserData(username, password, email));
    }

    @Test
    @DisplayName("Successfully login with valid credentials")
    void testLoginSuccess() throws DataAccessException, DatabaseServiceException {
        AuthData authData = loginService.login(username, password);

        assertNotNull(authData, "Auth data should not be null");
        assertEquals(username, authData.username(), "Username should match the logged in user");
        assertNotNull(authData.authToken(), "Auth token should not be null");

        AuthData storedAuth = authDAO.getAuthToken(authData.authToken());
        assertNotNull(storedAuth, "Auth token should be stored in the DAO");
        assertEquals(username, storedAuth.username(), "Stored username should match");
    }

    @Test
    @DisplayName("Fail to login with invalid username")
    void testLoginInvalidUsername() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                loginService.login("wrongUsername", password)
        );

        assertEquals("Error: unauthorized", exception.getMessage(), "Error message should indicate unauthorized");
    }

    @Test
    @DisplayName("Fail to login with invalid password")
    void testLoginInvalidPassword() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                loginService.login(username, "wrongPassword")
        );

        assertEquals("Error: unauthorized", exception.getMessage(), "Error message should indicate unauthorized");
    }
}