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

        while(true) {
            try {
                switch (currentREPLState) {
                    case PRELOGIN -> {
                        PreloginUIREPL preloginUIREPL = new PreloginUIREPL(server, scanner);
                        var result = preloginUIREPL.run();
                        if (result != null) {
                            authToken = result.authToken;
                            currentUser = result.username;
                            currentREPLState = REPLState.POSTLOGIN;
                        }
                    }
                    case POSTLOGIN -> {
                        PostloginUIREPL postloginUI = new PostloginUIREPL(server, scanner, authToken);
                        var result = postloginUI.run();
                    }

                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

        }
    }
}
