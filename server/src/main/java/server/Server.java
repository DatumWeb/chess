package server;

import server.handlers.*;
import service.*;
import spark.*;
import dataaccess.*;
import com.google.gson.Gson;
import websocket.WebSocketHandler;

import java.util.Map;

import static spark.Spark.webSocket;

public class Server {
    private ClearService clearService;
    private RegisterService registerService;
    private LoginService loginService;
    private LogoutService logoutService;
    private GameCreateService gameCreateService;
    private JoinGameService joinGameService;
    private GameListService gameListService;
    private final Gson gson = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");
        webSocket("/ws", WebSocketHandler.class);


        setupGlobalExceptionHandlers();

        try {
            DAOFactory.initializeDatabase();

            var userDAO = DAOFactory.createUserDAO();
            var gameDAO = DAOFactory.createGameDAO();
            var authDAO = DAOFactory.createAuthDAO();

            clearService = new ClearService(userDAO, gameDAO, authDAO);
            registerService = new RegisterService(userDAO, authDAO);
            loginService = new LoginService(userDAO, authDAO);
            logoutService = new LogoutService(authDAO);
            gameCreateService = new GameCreateService(gameDAO, authDAO);
            joinGameService = new JoinGameService(gameDAO, authDAO);
            gameListService = new GameListService(gameDAO, authDAO);

            Spark.delete("/db", new ClearHandler(clearService));
            Spark.delete("/session", new LogoutHandler(logoutService));
            Spark.post("/user", new RegisterHandler(registerService));
            Spark.post("/session", new LoginHandler(loginService));
            Spark.post("/game", new GameCreateHandler(gameCreateService));
            Spark.put("/game", new JoinGameHandler(joinGameService));
            Spark.get("/game", new GameListHandler(gameListService));

            Spark.awaitInitialization();
            return Spark.port();

        } catch (DataAccessException e) {
            System.err.println("Error initializing server: " + e.getMessage());
            Spark.stop();
            return -1;
        } catch (DatabaseServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupGlobalExceptionHandlers() {
        Spark.exception(DatabaseServiceException.class, (exception, request, response) -> {
            response.status(500);
            response.type("application/json");
            response.body(gson.toJson(Map.of("message", "Error: " + exception.getMessage())));
        });

        Spark.exception(DataAccessException.class, (exception, request, response) -> {
            String message = exception.getMessage();

            if (message != null && (message.contains("failed to get connection") ||
                    message.toLowerCase().contains("connection") ||
                    message.contains("communications link failure") ||
                    message.contains("database connection error"))) {
                response.status(500);
                response.type("application/json");
                response.body(gson.toJson(Map.of("message", "Error: " + message)));
                return;
            }

            // Handle specific business logic errors
            if (message != null && message.equals("Error: already taken")) {
                response.status(403);
                response.type("application/json");
                response.body(gson.toJson(Map.of("message", "Error: already taken")));
                return;
            }

            if (message != null && message.equals("Error: unauthorized")) {
                response.status(401);
                response.type("application/json");
                response.body(gson.toJson(Map.of("message", "Error: unauthorized")));
                return;
            }

            response.status(400);
            response.type("application/json");
            response.body(gson.toJson(Map.of("message", "Error: bad request")));
        });

        Spark.exception(Exception.class, (exception, request, response) -> {
            response.status(500);
            response.type("application/json");
            response.body(gson.toJson(Map.of("message", "Error: Internal Server Error")));
        });
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}