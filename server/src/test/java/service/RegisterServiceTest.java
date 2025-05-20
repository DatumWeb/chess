package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class RegisterServiceTest {
    private RegisterService registerService;
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private final String username = "newTestUser";
    private final String password = "testPassword";
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        registerService = new RegisterService(userDAO, authDAO);

        userDAO.createUser(new UserData("existingUser", "password", "existing@example.com"));
    }

    @Test
    @DisplayName("Successfully register new user")
    void testRegisterSuccess() throws DataAccessException {
        AuthData authData = registerService.register(username, password, email);

        assertNotNull(authData, "Auth data should not be null");
        assertEquals(username, authData.username(), "Username should match the registered user");
        assertNotNull(authData.authToken(), "Auth token should not be null");

        UserData storedUser = userDAO.getUser(username);
        assertNotNull(storedUser, "User should be stored in the DAO");
        assertEquals(username, storedUser.username(), "Stored username should match");
        assertEquals(password, storedUser.password(), "Stored password should match");
        assertEquals(email, storedUser.email(), "Stored email should match");

        AuthData storedAuth = authDAO.getAuthToken(authData.authToken());
        assertNotNull(storedAuth, "Auth token should be stored in the DAO");
        assertEquals(username, storedAuth.username(), "Stored username should match");
    }

    @Test
    @DisplayName("Fail to register with null username")
    void testRegisterNullUsername() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                registerService.register(null, password, email)
        );

        assertEquals("Username is null", exception.getMessage(), "Error message should indicate null username");
    }

    @Test
    @DisplayName("Fail to register with null password")
    void testRegisterNullPassword() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                registerService.register(username, null, email)
        );

        assertEquals("Password cannot be null.", exception.getMessage(), "Error message should indicate null password");
    }

    @Test
    @DisplayName("Fail to register with null email")
    void testRegisterNullEmail() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                registerService.register(username, password, null)
        );

        assertEquals("Email cannot be null.", exception.getMessage(), "Error message should indicate null email");
    }

    @Test
    @DisplayName("Fail to register with existing username")
    void testRegisterDuplicateUsername() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                registerService.register("existingUser", password, email)
        );

        assertEquals("Error: already taken", exception.getMessage(), "Error message should indicate username already taken");
    }
}