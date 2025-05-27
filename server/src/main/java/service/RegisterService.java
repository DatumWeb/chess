package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(String username, String password, String email) throws DataAccessException {
        if (username == null) {
            throw new DataAccessException("Username is null");
        }
        if (password == null) {
            throw new DataAccessException("Password cannot be null.");
        }
        if (email == null) {
            throw new DataAccessException("Email cannot be null.");
        }

        if (userDAO.getUser(username) != null) {
            throw new DataAccessException("Error: already taken");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        UserData newUser = new UserData(username, hashedPassword, email);
        userDAO.createUser(newUser);

        String authToken = generateToken();
        AuthData authData = new AuthData(authToken, username);
        authDAO.createAuthToken(authData);

        return authData;
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
