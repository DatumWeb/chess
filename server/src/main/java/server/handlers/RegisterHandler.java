package server.handlers;

import com.google.gson.Gson;
import service.RegisterService;
import model.AuthData;
import dataaccess.DataAccessException;
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
    public Object handle(Request request, Response response) {
        try {
            Map<String, String> body = gson.fromJson(request.body(), Map.class);
            String username = body.get("username");
            String password = body.get("password");
            String email = body.get("email");

            AuthData authData = registerService.register(username, password, email);

            response.status(200);
            return gson.toJson(authData);

        } catch (DataAccessException exception) {

            if (exception.getMessage().equals("Error: already taken")) {
                response.status(403);
                return gson.toJson(Map.of("message", "Error: already taken"));
            }
            response.status(400);
            return gson.toJson(Map.of("message", "Error: " + exception.getMessage()));
        } catch (Exception exception) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + exception.getMessage()));
        }
    }
}
