package ui;

import chess.ChessGame;
import websocket.WebSocketClientHandler;
import websocket.commands.*;
import websocket.messages.*;
import java.util.Scanner;
import chess.*;

public class GameplayUIREPL {
    private final WebSocketClientHandler webSocketClient;
    private final Scanner scanner;
    private final String authToken;
    private final Integer gameID;
    private final String playerColor;
    private ChessGame currentGame;

    public GameplayUIREPL(String serverUrl, String authToken, Integer gameID, String playerColor, WebSocketClientHandler webSocketClient) {
        this.webSocketClient = webSocketClient;
        this.playerColor = playerColor;
        this.scanner = new Scanner(System.in);
        this.authToken = authToken;
        this.gameID = gameID;

        webSocketClient.connect(serverUrl);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        sendConnectMessage();
        new Thread(this::processIncomingMessagesContinuously).start();

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
        processIncomingMessages();

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
        ServerMessage message;
        while ((message = webSocketClient.getNextMessage()) != null) {
            handleServerMessage(message);
        }
    }

    private void processIncomingMessagesContinuously() {
        while (true) {
            ServerMessage message = webSocketClient.getNextMessage();
            if (message != null) {
                handleServerMessage(message);
            }

            try {
                Thread.sleep(50); // Small delay to avoid CPU overload
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void handleServerMessage(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                LoadGameMessage loadGame = (LoadGameMessage) message;
                currentGame = loadGame.getGame();
                redrawBoard();
                break;
            case ERROR:
                ErrorMessage error = (ErrorMessage) message;
                System.err.println("Error: " + error.getErrorMessage());
                break;
            case NOTIFICATION:
                NotificationMessage notification = (NotificationMessage) message;
                System.out.println("[NOTIFICATION]: " + notification.getMessage());
                break;
            default:
                System.out.println("[SERVER]: Unknown message type: " + message.getServerMessageType());
        }
    }

    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("  move <from> <to> - make a move (e.g., 'move e2 e4')");
        System.out.println("  redraw - redraws the chess board");
        System.out.println("  leave - leave the game");
        System.out.println("  resign - resign the game");
        System.out.println("  highlight <position> - highlight legal moves for a piece (e.g., 'highlight e2')");
        System.out.println("  help - display this message");
    }

    private void redrawBoard() {
        if (currentGame != null) {
            boolean whitesPerspective = playerColor == null || playerColor.equals("WHITE");
            UIUtils.drawBoard(currentGame.getBoard(), whitesPerspective, null);
        } else {
            System.out.println("No game loaded yet. Please wait for the game to load.");
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

        if (currentGame == null) {
            System.err.println("Error: No game loaded. Please wait for the game to load.");
            return;
        }

        if (currentGame.isGameOver()) {
            System.err.println("Error: The game is over. No more moves allowed.");
            return;
        }

        try {
            ChessPosition start = parsePosition(inputTokens[1]);
            ChessPosition end = parsePosition(inputTokens[2]);

            ChessPiece piece = currentGame.getBoard().getPiece(start);
            if (piece == null) {
                System.err.println("Error: No piece at position " + inputTokens[1]);
                return;
            }

            if (piece.getTeamColor() != getPlayerTeamColor()) {
                System.err.println("Error: You can only move your own pieces!");
                return;
            }



            ChessGame.TeamColor currentTurn = currentGame.getTeamTurn();
            if (!isPlayersTurn(currentTurn)) {
                System.err.println("Error: It's not your turn!");
                return;
            }

            ChessPiece.PieceType promotionPiece = null;
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && end.getRow() == 8) ||
                        (piece.getTeamColor() == ChessGame.TeamColor.BLACK && end.getRow() == 1)) {

                    if (inputTokens.length > 3) {
                        promotionPiece = parsePromotionPiece(inputTokens[3]);
                    } else {
                        System.out.print("Pawn promotion! Enter piece (queen/rook/bishop/knight): ");
                        String promotionInput = scanner.nextLine().trim();
                        promotionPiece = parsePromotionPiece(promotionInput);
                    }
                }
            }

            ChessMove move = new ChessMove(start, end, promotionPiece);

            var validMoves = currentGame.validMoves(start);
            boolean isValidMove = validMoves.contains(move);

            if (!isValidMove) {
                System.err.println("Error: Invalid move! That move is not allowed.");
                System.out.println("Valid moves from " + inputTokens[1] + ":");
                for (ChessMove validMove : validMoves) {
                    System.out.println("  " + positionToString(validMove.getStartPosition()) +
                            " to " + positionToString(validMove.getEndPosition()));
                }
                return;
            }

            MakeMoveCommand moveCommand = new MakeMoveCommand(authToken, gameID, move);
            webSocketClient.sendMessage(moveCommand);

            System.out.println("Move sent: " + inputTokens[1] + " to " + inputTokens[2]);

        } catch (Exception e) {
            System.err.println("Error making move: " + e.getMessage());
        }
    }

    private boolean isPlayersTurn(ChessGame.TeamColor currentTurn) {
        ChessGame.TeamColor playerTeam = getPlayerTeamColor();
        return playerTeam != null && playerTeam == currentTurn;
    }

    private ChessGame.TeamColor getPlayerTeamColor() {
        if (playerColor == null) {
            return null;
        }
        return playerColor.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
    }

    private String positionToString(ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        return "" + col + pos.getRow();
    }


    private ChessPiece.PieceType parsePromotionPiece(String piece) {
        return switch (piece.toLowerCase()) {
            case "queen", "q" -> ChessPiece.PieceType.QUEEN;
            case "rook", "r" -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece: " + piece);
        };
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
            } else {
                System.out.println("No game loaded yet.");
            }
        } catch (Exception e) {
            System.err.println("Invalid position: " + e.getMessage());
        }
    }
}