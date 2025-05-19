package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.ClearService;
import spark.request;
import spark.Response;
import spark.Route;

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
        } catch (DataAccessException exception) {
            response.status(500);
            return "{\"message\": \"Error: " + exception.getMessage() + "\"}";
        }

    }

    private static class ClearResponse {
        private final String message;

        public ClearResponse(String message) {
            this.message = message;
        }
    }
}
