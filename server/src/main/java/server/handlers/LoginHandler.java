package server.handlers;

import com.google.gson.Gson;
import service.LoginService;
import model.AuthData;
import spark.Request;
import spark.Response;
import spark.Route;
import dataaccess.DataAccessException;
import dataaccess.DatabaseServiceException;

import java.util.Map;

public class LoginHandler implements Route {
    private final Gson gson = new Gson();
    private final LoginService loginService;

    public LoginHandler(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public Object handle(Request req, Response res) throws DataAccessException, DatabaseServiceException {
        Map<String, String> requestBody = gson.fromJson(req.body(), Map.class);
        String username = requestBody.get("username");
        String password = requestBody.get("password");

        if (username == null || password == null) {
            throw new DataAccessException("Error: bad request");
        }

        AuthData authData = loginService.login(username, password);

        res.status(200);
        return gson.toJson(Map.of("username", authData.username(), "authToken", authData.authToken()));
    }
}