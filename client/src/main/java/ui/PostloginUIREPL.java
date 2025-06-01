package ui;

import chess.*;
import java.util.Scanner;

public class PostloginUIREPL {
    private final ServerFacade server;
    private final Scanner scanner;
    private final String authToken;
    private ServerFacade.GameInfo[] gameList;

    public enum Result {
        LOGOUT, ENTER_GAME, CONTINUE
    }

    public PostloginUIREPL(ServerFacade server, Scanner scanner, String authToken) {
        this.server = server;
        this.scanner = scanner;
        this.authToken = authToken;
    }

    public Result run() {
        while (true) {
            System.out.print("[LOGGED_IN] >>> ");
            String input = scanner.nextLine().trim();
            String[] tokens = input.split("\\s+");
            String command = tokens.length > 0 ? tokens[0].toLowerCase() : "";

            try {
                switch (command) {
                    case "help" -> displayHelp();
                    case "logout" -> {
                        if (handleLogout()) {
                            return Result.LOGOUT;
                        }
                    }
                    case "create" -> handleCreateGame();
                    case "list" -> handleListGames();
                    case "join" -> {
                        if (handleJoinGame()) {
                            return Result.ENTER_GAME;
                        }
                    }
                    case "observe" -> {
                        if (handleObserveGame()) {
                            return Result.ENTER_GAME;
                        }
                    }
                    case "quit", "exit" -> {
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }
                    default -> System.out.println("Unknown command. Type 'help' for available commands.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}