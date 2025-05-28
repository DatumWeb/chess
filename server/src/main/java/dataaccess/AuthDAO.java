package dataaccess;

import model.AuthData;

public interface AuthDAO {
    AuthData getAuthToken(String authToken) throws DataAccessException, DatabaseServiceException;

    void createAuthToken(AuthData authData) throws DataAccessException, DatabaseServiceException;

    void deleteAuthToken(String authToken) throws DataAccessException, DatabaseServiceException;

    void clearAuth() throws DataAccessException, DatabaseServiceException;
}
