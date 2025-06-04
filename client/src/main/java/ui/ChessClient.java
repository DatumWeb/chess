package ui;

import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private final Scanner scanner;
    private REPLState currentREPLState;
    private String authToken;
    private String currentUser;

    private enum REPLState {
        PRELOGIN, POSTLOGIN, GAMEPLAY
    }

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        scanner = new Scanner(System.in);
        currentREPLState = REPLState.PRELOGIN;
    }

    public void run() {
        System.out.println("♕ Welcome to 240 Chess Client ♕");

        while (true) {
            try {
                switch (currentREPLState) {
                    case PRELOGIN -> handlePrelogin();
                    case POSTLOGIN -> handlePostlogin();
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
                currentREPLState = REPLState.GAMEPLAY;
                break;
            }
        } while (result == PostloginUIREPL.Result.CONTINUE);
    }

}
