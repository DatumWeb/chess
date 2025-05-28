package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseServiceException;
import service.ClearService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class ClearHandler implements Route {
    private final ClearService clearService;
    private final Gson gson = new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            clearService.clear();
            response.status(200);
            return "{}";
        } catch (DataAccessException | DatabaseServiceException exception) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + exception.getMessage()));
        }  catch (Exception exception) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }
}