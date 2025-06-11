package ui;

import websocket.WebSocketClientHandler;

import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private final WebSocketClientHandler webSocketClient;
    private final String httpServerUrl;
    private final String wsServerUrl;
    private final Scanner scanner;
    private REPLState currentREPLState;
    private String authToken;
    private String currentUser;
    private Integer currentGameID;
    private String currentPlayerColor;

    private enum REPLState {
        PRELOGIN, POSTLOGIN, GAMEPLAY
    }

    public ChessClient(String httpServerUrl, String wsServerUrl) {
        this.server = new ServerFacade(httpServerUrl);
        this.webSocketClient = new WebSocketClientHandler(wsServerUrl);
        this.httpServerUrl = httpServerUrl;
        this.wsServerUrl = wsServerUrl;
        this.scanner = new Scanner(System.in);
        this.currentREPLState = REPLState.PRELOGIN;
    }

    public void run() {
        System.out.println("♕ Welcome to 240 Chess Client ♕");

        while (true) {
            try {
                switch (currentREPLState) {
                    case PRELOGIN -> handlePrelogin();
                    case POSTLOGIN -> handlePostlogin();
                    case GAMEPLAY -> handleGameplay();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void handlePrelogin() throws Exception {
        PreloginUIREPL preloginUIREPL = new PreloginUIREPL(server, scanner);
        var result = preloginUIREPL.run();
        if (result != null) {
            authToken = result.authToken;
            currentUser = result.username;
            currentREPLState = REPLState.POSTLOGIN;
        }
    }

    private void handlePostlogin() throws Exception {
        PostloginUIREPL postloginUI = new PostloginUIREPL(server, scanner, authToken);
        PostloginUIREPL.Result result;

        do {
            result = postloginUI.run();

            if (result == PostloginUIREPL.Result.LOGOUT) {
                authToken = null;
                currentUser = null;
                currentREPLState = REPLState.PRELOGIN;
                break;
            } else if (result == PostloginUIREPL.Result.ENTER_GAME) {
                currentGameID = postloginUI.getSelectedGameID();
                currentPlayerColor = postloginUI.getSelectedPlayerColor();
                currentREPLState = REPLState.GAMEPLAY;
                break;
            }
        } while (result == PostloginUIREPL.Result.CONTINUE);
    }

    private void handleGameplay() throws Exception {
        GameplayUIREPL gameplayUI = new GameplayUIREPL(wsServerUrl, authToken, currentGameID, currentPlayerColor, webSocketClient);
        GameplayUIREPL.Result result = gameplayUI.run();

        if (result == GameplayUIREPL.Result.EXIT_GAME) {
            currentREPLState = REPLState.POSTLOGIN;
        } else if (result == GameplayUIREPL.Result.LOGOUT) {
            authToken = null;
            currentUser = null;
            currentREPLState = REPLState.PRELOGIN;
        }

        currentGameID = null;
        currentPlayerColor = null;
        System.out.println("You have left the game. Returning to the main menu.");
    }
}