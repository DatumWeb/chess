package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SQLAuthDAOTest {
    private SQLAuthDAO authDAO;

    @BeforeEach
    void setUp() throws DataAccessException, DatabaseServiceException {
        authDAO = new SQLAuthDAO();
        authDAO.clearAuth();
    }

    @Test
    @DisplayName("Successfully create and retrieve an auth token")
    void testCreateAndRetrieveAuthToken() throws DataAccessException, DatabaseServiceException {
        AuthData authData = new AuthData("validToken", "testUser");
        authDAO.createAuthToken(authData);

        AuthData retrievedAuth = authDAO.getAuthToken("validToken");

        assertNotNull(retrievedAuth, "Auth token should be retrievable after creation");
        assertEquals("testUser", retrievedAuth.username(), "Username should match");
    }

    @Test
    @DisplayName("Fail to retrieve non-existent auth token")
    void testGetNonExistentAuthToken() throws DatabaseServiceException, DataAccessException {
        assertNull(authDAO.getAuthToken("fakeToken"), "Retrieving a non-existent auth token should return null");
    }

    @Test
    @DisplayName("Successfully delete an auth token")
    void testDeleteAuthToken() throws DataAccessException, DatabaseServiceException {
        AuthData authData = new AuthData("deletableToken", "testUser");
        authDAO.createAuthToken(authData);

        authDAO.deleteAuthToken("deletableToken");

        assertNull(authDAO.getAuthToken("deletableToken"), "Auth token should be removed after deletion");
    }

    @Test
    @DisplayName("Fail to delete a non-existent auth token")
    void testDeleteNonExistentAuthToken() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> authDAO.deleteAuthToken("fakeToken"));

        assertEquals("Error: unauthorized", exception.getMessage(), "Deleting a non-existent auth token should return an unauthorized error");
    }

    @Test
    @DisplayName("Successfully clear all auth tokens")
    void testClearAuthTokens() throws DataAccessException, DatabaseServiceException {
        authDAO.createAuthToken(new AuthData("token1", "user1"));
        authDAO.createAuthToken(new AuthData("token2", "user2"));

        authDAO.clearAuth();

        assertNull(authDAO.getAuthToken("token1"), "Auth token 1 should be cleared");
        assertNull(authDAO.getAuthToken("token2"), "Auth token 2 should be cleared");
    }

    @Test
    @DisplayName("Successfully clear an empty auth token table")
    void testClearEmptyAuthTable() throws DataAccessException, DatabaseServiceException {
        authDAO.clearAuth();  // Already empty

        assertNull(authDAO.getAuthToken("nonexistentToken"), "Retrieving from cleared auth table should return null");
    }
}