package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public AuthData getAuthToken(String authToken) throws DataAccessException {
        return authTokens.getOrDefault(authToken, null);
    }

    @Override
    public void createAuthToken(AuthData authData) throws DataAccessException {
        if (authTokens.containsKey(authData.authToken())) {
            throw new DataAccessException("Error: Token already exists.");
        }
        authTokens.put(authData.authToken(), authData);
    }

    @Override
    public void deleteAuthToken(String authToken) throws DataAccessException {
        if (!authTokens.containsKey(authToken)) {
            throw new DataAccessException("Error: Token not found.");
        }
        authTokens.remove(authToken);
    }

    @Override
    public void clearAuth() {
        authTokens.clear();
    }
}
