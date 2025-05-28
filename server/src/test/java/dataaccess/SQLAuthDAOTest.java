package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SQLAuthDAOTest {
    private AuthDAO authDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        authDAO = new SQLAuthDAO();
        authDAO.deleteAuthToken(authDAO);  // Clean start before each test
    }

    @Test
    @DisplayName("Successfully create and retrieve an auth token")
    void testCreateAndRetrieveAuth() throws DataAccessException, DatabaseServiceException {
        AuthData auth = new AuthData("validToken", "testUser");
        authDAO.createAuthToken(auth);

        AuthData retrievedAuth = authDAO.getAuthToken("validToken");

        assertNotNull(retrievedAuth, "Auth token should be retrievable after creation");
        assertEquals("testUser", retrievedAuth.username(), "Username should match");
    }

    @Test
    @DisplayName("Fail to retrieve non-existent auth token")
    void testGetNonExistentAuth() throws DatabaseServiceException, DataAccessException {
        assertNull(authDAO.getAuthToken("fakeToken"), "Retrieving a non-existent auth token should return null");
    }

    @Test
    @DisplayName("Successfully delete an auth token")
    void testDeleteAuthToken() throws DataAccessException, DatabaseServiceException {
        AuthData auth = new AuthData("deletableToken", "testUser");
        authDAO.createAuthToken(auth);

        authDAO.deleteAuthToken("deletableToken");

        assertNull(authDAO.getAuthToken("deletableToken"), "Auth token should be removed after deletion");
    }
}