package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> userDataMap = new HashMap<>();

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return userDataMap.get(username);
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        if (userDataMap.containsKey(userData.username())) {
            throw new DataAccessException("User already exists");
        }
        userDataMap.put(userData.username(), userData);
    }


    @Override
    public void clearUser() {
        userDataMap.clear();
    }
}
