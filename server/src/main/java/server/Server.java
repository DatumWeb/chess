package server;


import server.handlers.*;
import service.*;
import spark.*;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;

public class Server {
    private ClearService clearService;
    private RegisterService registerService;
    private LoginService loginService;
    private LogoutService logoutService;
    private GameCreateService gameCreateService;
    private JoinGameService joinGameService;
    private GameListService gameListService;


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        var userDAO = new MemoryUserDAO();
        var gameDAO = new MemoryGameDAO();
        var authDAO = new MemoryAuthDAO();

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
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}