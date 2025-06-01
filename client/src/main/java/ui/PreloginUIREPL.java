package ui;

import java.util.Scanner;

public class PreloginUIREPL {
    private final ServerFacade server;
    private final Scanner scanner;

    public PreloginUIREPL(ServerFacade server, Scanner scanner) {
        this.server = server;
        this.scanner = scanner;
    }

    public ServerFacade.AuthResult run() {
        while (true) {
            System.out.print("[LOGGED_OUT] >>> ");
            String input = scanner.nextLine().trim();
            String[] inputTokens = input.split("\\s+");
            String command = inputTokens.length > 0 ? inputTokens[0].toLowerCase() : "";

            try {
                switch (command) {
                    case "help" -> displayHelp();
                    case "quit", "exit" -> {
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }
                    case "login" -> {
                        var result = handleLogin(inputTokens);
                        if (result != null) {
                            return result;
                        }
                    }
                    case "register" -> {
                        var result = handleRegister(inputTokens);
                        if (result != null) {
                            return result;
                        }
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
        System.out.println("  register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
        System.out.println("  login <USERNAME> <PASSWORD> - to play chess");
        System.out.println("  quit - to exit");
        System.out.println("  help - to display this message");
    }

    private ServerFacade.AuthResult handleLogin(String[] inputTokens) {
        if (inputTokens.length < 3) {
            System.err.println("Error: Should be: login <Your Username> <Your Password> ");
            return null;
        }

        String username = inputTokens[1];
        String password = inputTokens[2];

        try {
            var result = server.login(username, password);
            System.out.println("Successfully logged in as " + result.username);
            return result;
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    private ServerFacade.AuthResult handleRegister(String[] inputTokens) {
        if (inputTokens.length < 4) {
            System.err.println("Error: Should be: register <Your Username> <Your Password> <Your Email> ");
            return null;
        }

        String username = inputTokens[1];
        String password = inputTokens[2];
        String email = inputTokens[3];

        try {
            var result = server.register(username, password, email);
            System.out.println("Successfully registered and logged in as " + result.username);
            return result;
        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
            return null;
        }
    }
}