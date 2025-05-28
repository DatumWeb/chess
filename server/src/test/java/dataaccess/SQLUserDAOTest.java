package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SQLUserDAOTest {
    private SQLUserDAO userDAO;

    @BeforeEach
    void setUp() throws DataAccessException, DatabaseServiceException {
        userDAO = new SQLUserDAO();
        userDAO.clearUser();
    }

    @Test
    @DisplayName("Successfully create and retrieve a user")
    void testCreateAndRetrieveUser() throws DataAccessException, DatabaseServiceException {
        UserData user = new UserData("testUser", "securePass123", "email@example.com");
        userDAO.createUser(user);

        UserData retrievedUser = userDAO.getUser("testUser");

        assertNotNull(retrievedUser, "User should be retrievable after creation");
        assertEquals("securePass123", retrievedUser.password(), "Password should match");
        assertEquals("email@example.com", retrievedUser.email(), "Email should match");
    }

    @Test
    @DisplayName("Fail to retrieve non-existent user")
    void testGetNonExistentUser() throws DatabaseServiceException, DataAccessException {
        assertNull(userDAO.getUser("fakeUser"), "Retrieving a non-existent user should return null");
    }

    @Test
    @DisplayName("Fail to create a duplicate user")
    void testDuplicateUserCreation() throws DataAccessException, DatabaseServiceException {
        UserData user = new UserData("duplicateUser", "securePass", "email@domain.com");
        userDAO.createUser(user);

        DataAccessException exception = assertThrows(DataAccessException.class, () -> userDAO.createUser(user));

        assertEquals("Error: already taken", exception.getMessage(), "Duplicate username should trigger an exception");
    }

    @Test
    @DisplayName("Fail to create a user with missing data")
    void testCreateUserWithNullValues() {
        UserData invalidUser = new UserData(null, "password123", "email@example.com");

        assertThrows(DataAccessException.class, () -> userDAO.createUser(invalidUser), "Creating a user without a username should fail");
    }

    @Test
    @DisplayName("Successfully clear all users")
    void testClearUsers() throws DataAccessException, DatabaseServiceException {
        userDAO.createUser(new UserData("User1", "Pass1", "email1@gmail.com"));
        userDAO.createUser(new UserData("User2", "Pass2", "email2@gmail.com"));

        userDAO.clearUser();

        assertNull(userDAO.getUser("User1"), "User1 should be cleared");
        assertNull(userDAO.getUser("User2"), "User2 should be cleared");
    }

    @Test
    @DisplayName("Successfully clear an empty user table")
    void testClearEmptyUserTable() throws DataAccessException, DatabaseServiceException {
        userDAO.clearUser();

        assertNull(userDAO.getUser("nonexistentUser"), "Retrieving from cleared user table should return null");
    }
}