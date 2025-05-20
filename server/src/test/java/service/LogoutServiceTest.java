package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class LogoutServiceTest {
    private LogoutService logoutService;
    private AuthDAO authDAO;
    private String validAuthToken;
    private String invalidAuthToken;
    private final String username = "testUser";

    @BeforeEach
    void setUp() throws DataAccessException {
        authDAO = new MemoryAuthDAO();
        logoutService = new LogoutService(authDAO);

        AuthData authData = new AuthData("validToken", username);
        authDAO.createAuthToken(authData);
        validAuthToken = authData.authToken();
        invalidAuthToken = "invalidToken";
    }

    @Test
    @DisplayName("Successfully logout with valid auth token")
    void testLogoutSuccess() throws DataAccessException {
        assertNotNull(authDAO.getAuthToken(validAuthToken), "Auth token should exist before logout");

        boolean result = logoutService.logout(validAuthToken);

        assertTrue(result, "Logout should return true");
        assertNull(authDAO.getAuthToken(validAuthToken), "Auth token should be removed after logout");
    }

    @Test
    @DisplayName("Fail to logout with invalid auth token")
    void testLogoutInvalidToken() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                logoutService.logout(invalidAuthToken)
        );

        assertEquals("Error: unauthorized", exception.getMessage(), "Error message should indicate unauthorized");
    }

    @Test
    @DisplayName("Fail to logout with null auth token")
    void testLogoutNullToken() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                logoutService.logout(null)
        );

        assertEquals("Auth token is missing.", exception.getMessage(), "Error message should indicate missing token");
    }
}