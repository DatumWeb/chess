package server;


import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

public class ClearHandler implements Route {
    @Override
    public Object handle(Request req, Response res) {
        try {
            ClearService clearService = new ClearService();
            clearService.clearAll(); // Call the service layer to clear data

            res.status(200);
            return "{}"; // Return empty JSON response for success
        } catch (Exception e) {
            res.status(500);
            return new Gson().toJson(new ErrorResponse("Internal Server Error"));
        }
    }
}
