package server.handlers;

import com.google.gson.Gson;
import service.RegisterService;
import model.AuthData;
import dataaccess.DataAccessException;
import dataaccess.DatabaseServiceException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class RegisterHandler implements Route {
    private final RegisterService registerService;
    private final Gson gson = new Gson();

    public RegisterHandler(RegisterService registerService) {
        this.registerService = registerService;
    }

    @Override
    public Object handle(Request request, Response response) throws DataAccessException, DatabaseServiceException {
        Map<String, String> body = gson.fromJson(request.body(), Map.class);
        String username = body.get("username");
        String password = body.get("password");
        String email = body.get("email");

        AuthData authData = registerService.register(username, password, email);

        response.status(200);
        return gson.toJson(authData);
    }
}