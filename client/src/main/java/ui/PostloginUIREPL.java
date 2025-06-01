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
                        if (handleJoinGame()) { //fix this
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

    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("  create <NAME> - a game");
        System.out.println("  list - games");
        System.out.println("  join <ID> [WHITE|BLACK] - a game");
        System.out.println("  observe <ID> - a game");
        System.out.println("  logout - when you are done");
        System.out.println("  quit - to exit");
        System.out.println("  help - to display this message");
    }

    private boolean handleLogout() {
        try {
            server.logout(authToken);
            System.out.println("Successfully logged out.");
            return true;
        } catch (Exception e) {
            System.err.println("Logout failed: " + e.getMessage());
            return false;
        }
    }

    private void handleCreateGame() {
        System.out.print("Game name: ");
        String gameName = scanner.nextLine().trim();

        if (gameName.isEmpty()) {
            System.err.println("Game name cannot be empty.");
            return;
        }

        try {
            server.createGame(gameName, authToken);
            System.out.println("Game '" + gameName + "' created successfully.");
        } catch (Exception e) {
            System.err.println("Failed to create game: " + e.getMessage());
        }
    }

    private void handleListGames() {
        try {
            var result = server.listGames(authToken);
        } catch (Exception e) {
            System.err.println("Failed to list games: " + e.getMessage());
        }
    }

    private boolean handleJoinGame() {
        if (gameList == null || gameList.length == 0) {
            System.err.println("No games available. Use 'list' to see available games.");
            return false;
        }


        return false;
    }

}