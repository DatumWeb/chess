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
            String[] tokens = input.split("\\s+");
            String command = tokens.length > 0 ? tokens[0].toLowerCase() : "";

            try {
                switch (command) {
                    case "help" -> displayHelp();
                    case "quit", "exit" -> {
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }
                    case "login" -> {
                        var result = handleLogin();
                        if (result != null) {
                            return result;
                        }
                    }
                    case "register" -> {
                        var result = handleRegister();
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

    private ServerFacade.AuthResult handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.err.println("Username and password cannot be empty.");
            return null;
        }

        try {
            var result = server.login(username, password);
            System.out.println("Successfully logged in as " + result.username);
            return result;
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    private ServerFacade.AuthResult handleRegister() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            System.err.println("All fields are required.");
            return null;
        }

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