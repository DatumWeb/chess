package dataaccess;

import model.UserData;

import javax.xml.crypto.Data;

public interface UserDAO {
    UserData getUser(String username) throws DataAccessException, DatabaseServiceException;

    void createUser(UserData userData) throws DataAccessException, DatabaseServiceException;

    void clearUser() throws DataAccessException, DatabaseServiceException;

}
