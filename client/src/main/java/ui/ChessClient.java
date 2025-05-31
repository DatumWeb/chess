package ui;

import java.util.Scanner;

public class ChessClient {
    private final String serverUrl;
    private final ServerFacade server;
    private final Scanner scanner;
    private REPLState currentREPLState;

    private enum REPLState {
        PRELOGIN, POSTLOGIN, GAMEPLAY
    }

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.scanner = new Scanner(System.in);
        currentREPLState = REPLState.PRELOGIN;
    }

    public void run() {
        System.out.println("♕ Welcome to 240 Chess Client ♕");

        while(true) {
            try {
                switch (currentREPLState) {
                    case PRELOGIN -> {
                        PreloginUIREPL preloginUIREPL = PreloginUIREPL(server, scanner);
                        var result = preloginUIREPL.run();
                    }
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

        }
    }
}
