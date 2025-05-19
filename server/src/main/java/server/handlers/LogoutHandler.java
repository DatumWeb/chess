package server.handlers;

import com.google.gson.Gson;
import service.LogoutService;
import dataaccess.DataAccessException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class LogoutHandler implements Route {
    private final LogoutService logoutService;
    private final Gson gson = new Gson();

    public LogoutHandler(LogoutService logoutService) {
        this.logoutService = logoutService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            String authToken = request.headers("authorization");

            if (authToken == null) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            boolean success = logoutService.logout(authToken);

            if (!success) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            response.status(200);
            return "{}";

        } catch (DataAccessException exception) {
            response.status(401);
            return gson.toJson(Map.of("message", exception.getMessage()));
        } catch (Exception exception) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + exception.getMessage()));
        }
    }
}
