package server.handlers;

import com.google.gson.Gson;
import service.JoinGameService;
import dataaccess.DataAccessException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class JoinGameHandler implements Route {
    private final JoinGameService joinGameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(JoinGameService joinGameService) {
        this.joinGameService = joinGameService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            String authToken = request.headers("authorization");
            Map<String, Object> body = gson.fromJson(request.body(), Map.class);
            String playerColor = (String) body.get("playerColor");

            Number gameIDNum = (Number) body.get("gameID");
            if (gameIDNum == null) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            int gameID = gameIDNum.intValue();

            joinGameService.joinGame(authToken, gameID, playerColor);

            response.status(200);
            return gson.toJson(Map.of());

        } catch (DataAccessException exception) {
            if (exception.getMessage().equals("Error: unauthorized")) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));

            } else if (exception.getMessage().equals("Error: bad request")) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));

            } else if (exception.getMessage().equals("Error: already taken")) {
                response.status(403);
                return gson.toJson(Map.of("message", "Error: already taken"));
            }

            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + exception.getMessage()));
        } catch (Exception exception) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + exception.getMessage()));
        }
    }
}
