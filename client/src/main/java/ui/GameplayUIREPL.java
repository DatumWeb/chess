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

    private void redrawBoard() {
        if (currentGame != null) {
            boolean whitesPerspective = playerColor == null || playerColor.equals("WHITE");
            drawBoard(currentGame.getBoard(), whitesPerspective, null);
        }
    }

    //this is rough, will refractor to utils to stop code duplication.
    private void drawBoard(ChessBoard board, boolean whitesPerspective,
                           java.util.Collection<ChessMove> highlightMoves) {
        System.out.println();

        int[] rows = whitesPerspective ? new int[]{8,7,6,5,4,3,2,1} : new int[]{1,2,3,4,5,6,7,8};
        int[] cols = whitesPerspective ? new int[]{1,2,3,4,5,6,7,8} : new int[]{8,7,6,5,4,3,2,1};
        char[] colLabels = whitesPerspective ? new char[]{'a','b','c','d','e','f','g','h'} :
                new char[]{'h','g','f','e','d','c','b','a'};

        System.out.print("   ");
        for (char col : colLabels) {
            System.out.print(" " + col + " ");
        }
        System.out.println();

        for (int row : rows) {
            System.out.print(" " + row + " ");

            for (int col : cols) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                boolean isHighlighted = isHighlightedPosition(pos, highlightMoves);
                boolean isLight = (row + col) % 2 == 0;

                String bgColor;
                if (isHighlighted) {
                    bgColor = EscapeSequences.SET_BG_COLOR_YELLOW;
                } else {
                    bgColor = isLight ? EscapeSequences.SET_BG_COLOR_MAGENTA : EscapeSequences.SET_BG_COLOR_WHITE;
                }

                String textColor = piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.SET_TEXT_COLOR_RED
                        : EscapeSequences.SET_TEXT_COLOR_BLACK;

                String pieceStr = getPieceString(piece);
                System.out.print(bgColor + textColor + pieceStr +
                        EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR);
            }

            System.out.println(" " + row);
        }

        System.out.print("   ");
        for (char col : colLabels) {
            System.out.print(" " + col + " ");
        }
        System.out.println();
    }

    private boolean isHighlightedPosition(ChessPosition pos, java.util.Collection<ChessMove> highlightMoves) {
        if (highlightMoves == null) return false;

        return highlightMoves.stream().anyMatch(move ->
                move.getStartPosition().equals(pos) || move.getEndPosition().equals(pos));
    }

    private String getPieceString(ChessPiece piece) {
        if (piece == null) {
            return "   ";
        }

        char pieceChar = switch (piece.getPieceType()) {
            case KING -> 'K';
            case QUEEN -> 'Q';
            case BISHOP -> 'B';
            case KNIGHT -> 'N';
            case ROOK -> 'R';
            case PAWN -> 'P';
        };

        return " " + pieceChar + " ";
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
}