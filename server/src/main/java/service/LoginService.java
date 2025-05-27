package service;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class LoginService {
    private final UserDAO userDAO;
    private final AuthDAO authDao;

    public LoginService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDao = authDAO;
    }

    public AuthData login(String username, String password) throws DataAccessException {
        UserData user = userDAO.getUser(username);
        if (user == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (!user.password().equals(password)) {
            throw new DataAccessException("Error: unauthorized");
        }

        AuthData authData = new AuthData(generateToken(), username);
        authDao.createAuthToken(authData);
        return authData;
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }


}
