package dataaccess;

import model.AuthData;

public interface AuthDAO {
    AuthData getAuthToken(String authToken) throws DataAccessException;

    void createAuthToken(AuthData authData) throws DataAccessException;

    void deleteAuthToken(String authToken) throws DataAccessException;

    void clearAuth() throws DataAccessException;
}
