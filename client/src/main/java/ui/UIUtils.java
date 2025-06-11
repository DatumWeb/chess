package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import java.util.Collection;

public class UIUtils {

    public static void drawBoard(ChessBoard board, boolean whitesPerspective, Collection<ChessMove> highlightMoves) {
        System.out.println();

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

                boolean isHighlighted = isHighlightedPosition(pos, highlightMoves);
                boolean isLight = (row + col) % 2 == 0;

                String bgColor = isHighlighted ? EscapeSequences.SET_BG_COLOR_YELLOW
                        : isLight ? EscapeSequences.SET_BG_COLOR_MAGENTA
                        : EscapeSequences.SET_BG_COLOR_WHITE;

                String textColor = (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                        ? EscapeSequences.SET_TEXT_COLOR_RED
                        : EscapeSequences.SET_TEXT_COLOR_BLACK;

                System.out.print(bgColor + textColor + getPieceString(piece) + EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR);
            }

            System.out.println(" " + row);
        }

        System.out.print("   ");
        for (char col : colLabels) {
            System.out.print(" " + col + " ");
        }
        System.out.println();
    }

    public static boolean isHighlightedPosition(ChessPosition pos, Collection<ChessMove> highlightMoves) {
        if (highlightMoves == null) {
            return false;
        }
        return highlightMoves.stream().anyMatch(move ->
                move.getStartPosition().equals(pos) || move.getEndPosition().equals(pos));
    }

    public static String getPieceString(ChessPiece piece) {
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