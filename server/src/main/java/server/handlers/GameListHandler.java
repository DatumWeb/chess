package server.handlers;

import com.google.gson.Gson;
import service.GameListService;
import dataaccess.DataAccessException;
import model.GameData;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GameListHandler implements Route {
    private final GameListService gameListService;
    private final Gson gson = new Gson();
    private static final Logger LOGGER = Logger.getLogger(GameListHandler.class.getName());

    public GameListHandler(GameListService gameListService) {
        this.gameListService = gameListService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            String authToken = request.headers("authorization");
            LOGGER.info("Auth Token: " + authToken);

            List<GameData> games = gameListService.getGameList(authToken);

            response.status(200);
            return gson.toJson(Map.of("games", games));

        } catch (DataAccessException exception) {
            LOGGER.severe("Data Access Error: " + exception.getMessage());
            if (exception.getMessage().equals("Error: unauthorized")) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + exception.getMessage()));
        } catch (Exception exception) {
            LOGGER.severe("General Error: " + exception.getMessage());
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + exception.getMessage()));
        }
    }
}
