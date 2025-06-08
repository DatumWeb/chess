package ui;

import websocket.WebSocketClientHandler;
import websocket.commands.*;
import java.util.Scanner;

public class GameplayUIREPL {
    private final WebSocketClientHandler webSocketClient;
    private final Scanner scanner;
    private final String authToken;
    private final Integer gameID;

    public GameplayUIREPL(String serverUrl, String authToken, Integer gameID) {
        this.webSocketClient = new WebSocketClientHandler(serverUrl);
        this.scanner = new Scanner(System.in);
        this.authToken = authToken;
        this.gameID = gameID;

        sendConnectMessage();
    }

    private void sendConnectMessage() {
        UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        webSocketClient.sendMessage(connectCommand);
        System.out.println("Connected to the game. Type 'help' for available commands.");
    }

    public void run() {
        while (true) {
            System.out.print("[GAMEPLAY] >>> ");
            String input = scanner.nextLine().trim();
            String[] inputTokens = input.split("\\s+");
            String command = inputTokens.length > 0 ? inputTokens[0].toLowerCase() : "";

            try {
                processCommand(command, inputTokens);
                processIncomingMessages();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private void processCommand(String command, String[] inputTokens) {
        switch (command) {
            case "help" -> displayHelp();
            case "redraw" -> redrawBoard();
            case "leave" -> sendLeaveMessage();
            case "resign" -> sendResignMessage();
            case "move" -> handleMove(inputTokens);
            case "highlight" -> handleHighlight(inputTokens);
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void processIncomingMessages() {
        var message = webSocketClient.getNextMessage();
        if (message != null) {
            System.out.println("[SERVER]: " + message.getServerMessageType());
            // Extend message processing based on type
        }
    }

    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("  move <startRow> <startCol> <endRow> <endCol> - make a move");
        System.out.println("  redraw - redraws the chess board");
        System.out.println("  leave - leave the game");
        System.out.println("  resign - resign the game");
        System.out.println("  highlight <row> <col> - highlight legal moves for a piece");
        System.out.println("  help - display this message");
    }



}