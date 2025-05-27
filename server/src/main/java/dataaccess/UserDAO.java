package dataaccess;

import model.UserData;

import javax.xml.crypto.Data;

public interface UserDAO {
    UserData getUser(String username) throws DataAccessException;

    void createUser(UserData userData) throws DataAccessException;

    void clearUser() throws DataAccessException;

}
