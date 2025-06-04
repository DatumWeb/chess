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
            String[] inputTokens = input.split("\\s+");
            String command = inputTokens.length > 0 ? inputTokens[0].toLowerCase() : "";

            try {
                return processCommand(command, inputTokens);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private Result processCommand(String command, String[] inputTokens) throws Exception {
        switch (command) {
            case "help":
                displayHelp();
                return Result.CONTINUE;
            case "logout":
                return handleLogout() ? Result.LOGOUT : Result.CONTINUE;
            case "create":
                handleCreateGame(inputTokens);
                return Result.CONTINUE;
            case "list":
                handleListGames();
                return Result.CONTINUE;
            case "join":
                return handleJoinGame(inputTokens) ? Result.ENTER_GAME : Result.CONTINUE;
            case "observe":
                return handleObserveGame(inputTokens) ? Result.ENTER_GAME : Result.CONTINUE;
            case "quit":
            case "exit":
                System.out.println("Goodbye!");
                System.exit(0);
                return Result.CONTINUE;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
                return Result.CONTINUE;
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

    private void handleCreateGame(String[] inputTokens) {
        if (inputTokens.length < 2) {
            System.err.println("Error: Should be: create <Game Name>");
            return;
        }

        String gameName = inputTokens[1];

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
            gameList = result.games;

            if (gameList.length == 0) {
                System.out.println("No games available.");
                return;
            }

            System.out.println("Available games:");
            for (int i = 0; i < gameList.length; i++) {
                var game = gameList[i];
                String white = game.whiteUsername != null ? game.whiteUsername : "empty";
                String black = game.blackUsername != null ? game.blackUsername : "empty";
                System.out.printf("%d. %s - White: %s, Black: %s%n",
                        i + 1, game.gameName, white, black);
            }
        } catch (Exception e) {
            System.err.println("Failed to list games: " + e.getMessage());
        }
    }

    private boolean handleJoinGame(String[] inputTokens) {
        if (!validateGameList()) {
            return false;
        }
        if (!validateJoinInputs(inputTokens)) {
            return false;
        }

        int gameNum = Integer.parseInt(inputTokens[1]);
        String color = inputTokens[2].toUpperCase();
        var game = gameList[gameNum - 1];

        return tryJoiningGame(game, color);
    }

    private boolean validateGameList() {
        if (gameList == null || gameList.length == 0) {
            System.err.println("No games available. Use 'list' to see available games.");
            return false;
        }
        return true;
    }

    private boolean validateJoinInputs(String[] inputTokens) {
        if (inputTokens.length < 3) {
            System.err.println("Error: Should be: join <Game Number> <WHITE/BLACK>");
            return false;
        }
        return true;
    }

    private boolean tryJoiningGame(ServerFacade.GameInfo game, String color) {
        try {
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.err.println("Color must be WHITE or BLACK.");
                return false;
            }
            server.joinGame(game.gameID, color, authToken);
            System.out.println("Successfully joined game as " + color);
            drawChessBoard(color.equals("WHITE"));
            return true;
        } catch (Exception e) {
            System.err.println("Failed to join game: " + e.getMessage());
            return false;
        }
    }

    private boolean handleObserveGame(String[] inputTokens) {
        if (inputTokens.length < 2) {
            System.err.println("Error: Should be: observe <Game Number>");
            return false;
        }

        if (gameList == null || gameList.length == 0) {
            System.err.println("No games available. Use 'list' to see available games.");
            return false;
        }

        try {
            int gameNum = Integer.parseInt(inputTokens[1]);
            if (gameNum < 1 || gameNum > gameList.length) {
                System.err.println("Invalid game number. Must be between 1 and " + gameList.length);
                return false;
            }

            System.out.println("Observing game...");

            drawChessBoard(true);

            return true;

        } catch (NumberFormatException e) {
            System.err.println("Game number must be a valid integer.");
            return false;
        } catch (Exception e) {
            System.err.println("Failed to observe game: " + e.getMessage());
            return false;
        }
    }


    private void drawChessBoard(boolean whitesPerspective) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        System.out.println();
        drawBoard(board, whitesPerspective);
        System.out.println();
    }

    private void drawBoard(ChessBoard board, boolean whitesPerspective) {
        int[] rows = whitesPerspective ? new int[]{8,7,6,5,4,3,2,1} : new int[]{1,2,3,4,5,6,7,8};
        int[] cols = whitesPerspective ? new int[]{1,2,3,4,5,6,7,8} : new int[]{8,7,6,5,4,3,2,1};
        char[] colLabels = whitesPerspective ? new char[]{'a','b','c','d','e','f','g','h'} : new char[]{'h','g','f','e','d','c','b','a'};

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

                boolean isLight = (row + col) % 2 == 0;
                String bgColor = isLight ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_MAGENTA;
                String textColor = piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.SET_TEXT_COLOR_RED
                        : EscapeSequences.SET_TEXT_COLOR_BLACK;

                String pieceStr = getPieceString(piece);
                System.out.print(bgColor + textColor + pieceStr + EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR);
            }

            System.out.println(" " + row);
        }

        System.out.print("   ");
        for (char col : colLabels) {
            System.out.print(" " + col + " ");
        }
        System.out.println();
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




}