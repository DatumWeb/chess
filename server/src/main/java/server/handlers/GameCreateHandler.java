package server.handlers;

import com.google.gson.Gson;
import service.GameCreateService;
import dataaccess.DataAccessException;
import model.GameData;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class GameCreateHandler implements Route {
    private final GameCreateService gameCreateService;
    private final Gson gson = new Gson();

    public GameCreateHandler(GameCreateService gameCreateService) {
        this.gameCreateService = gameCreateService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            String authToken = request.headers("authorization");
            Map<String, String> body = gson.fromJson(request.body(), Map.class);
            String gameName = body.get("gameName");

            GameData gameData = gameCreateService.createGame(authToken, gameName);

            response.status(200);
            return gson.toJson(Map.of("gameID", gameData.gameID()));

        } catch (DataAccessException exception) {
            if (exception.getMessage().equals("Error: unauthorized")) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            } else if (exception.getMessage().equals("Error: bad request")) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + exception.getMessage()));
        } catch (Exception exception) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + exception.getMessage()));
        }
    }
}
