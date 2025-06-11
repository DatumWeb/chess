package ui;

import chess.ChessGame;
import websocket.WebSocketClientHandler;
import websocket.commands.*;
import java.util.Scanner;
import chess.*;

public class GameplayUIREPL {
    private final WebSocketClientHandler webSocketClient;
    private final Scanner scanner;
    private final String authToken;
    private final Integer gameID;
    private final String playerColor;
    private ChessGame currentGame;

    public GameplayUIREPL(String serverUrl, String authToken, Integer gameID, String playerColor) {
        this.webSocketClient = new WebSocketClientHandler(serverUrl);
        this.playerColor = playerColor;
        this.scanner = new Scanner(System.in);
        this.authToken = authToken;
        this.gameID = gameID;

        sendConnectMessage();
    }

    public enum Result {
        EXIT_GAME,
        LOGOUT,
        CONTINUE
    }

    private void sendConnectMessage() {
        UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        webSocketClient.sendMessage(connectCommand);
        System.out.println("Connected to the game. Type 'help' for available commands.");
    }

    public Result run() {
        System.out.print("[GAMEPLAY] >>> ");
        String input;
        while ((input = scanner.nextLine()) != null) {
            String[] tokens = input.trim().split("\\s+");
            String command = tokens.length > 0 ? tokens[0].toLowerCase() : "";

            if (command.equals("leave")) {
                sendLeaveMessage();
                return Result.EXIT_GAME;
            }
            if (command.equals("logout")) {
                return Result.LOGOUT;
            }

            try {
                processCommand(command, tokens);
                processIncomingMessages();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
            System.out.print("[GAMEPLAY] >>> ");
        }
        return Result.EXIT_GAME;
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

    private void redrawBoard() {
        if (currentGame != null) {
            boolean whitesPerspective = playerColor == null || playerColor.equals("WHITE");
            UIUtils.drawBoard(currentGame.getBoard(), whitesPerspective, null);
        }
    }

    private void sendLeaveMessage() {
        UserGameCommand leaveCommand = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        webSocketClient.sendMessage(leaveCommand);
        System.out.println("Left the game.");
    }

    private void sendResignMessage() {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (confirmation.equals("yes") || confirmation.equals("y")) {
            UserGameCommand resignCommand = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            webSocketClient.sendMessage(resignCommand);
            System.out.println("Resigned from the game.");
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void handleMove(String[] inputTokens) {
        if (inputTokens.length < 3) {
            System.err.println("Usage: move <from> <to> (example: 'move a2 a4')");
            return;
        }

        try {
            ChessPosition start = parsePosition(inputTokens[1]);
            ChessPosition end = parsePosition(inputTokens[2]);

            ChessMove move = new ChessMove(start, end, null);

            MakeMoveCommand moveCommand = new MakeMoveCommand(authToken, gameID, move);
            webSocketClient.sendMessage(moveCommand);
        } catch (Exception e) {
            System.err.println("Invalid move format: " + e.getMessage());
        }
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Position must be 2 characters (e.g., 'e2')");
        }

        char colChar = pos.charAt(0);
        char rowChar = pos.charAt(1);

        int col = colChar - 'a' + 1;
        int row = rowChar - '0';

        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Position out of bounds");
        }

        return new ChessPosition(row, col);
    }

    private void handleHighlight(String[] inputTokens) {
        if (inputTokens.length < 2) {
            System.err.println("Usage: highlight <position> (e.g., 'highlight e2')");
            return;
        }

        try {
            ChessPosition position = parsePosition(inputTokens[1]);

            if (currentGame != null) {
                var validMoves = currentGame.validMoves(position);
                boolean whitesPerspective = playerColor == null || playerColor.equals("WHITE");
                UIUtils.drawBoard(currentGame.getBoard(), whitesPerspective, validMoves);
            }
        } catch (Exception e) {
            System.err.println("Invalid position: " + e.getMessage());
        }
    }
}