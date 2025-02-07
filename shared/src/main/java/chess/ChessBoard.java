package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] boardGrid;

    public ChessBoard() {
        boardGrid = new ChessPiece[8][8];

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        boardGrid[position.getRow() - 1][position.getColumn() - 1] = piece;


    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return boardGrid[position.getRow() - 1][position.getColumn() - 1];
    }

    public ChessGame.TeamColor getTeamOnSquare(ChessPosition position) {
        if (getPiece(position) == null) {
            return null;
        } else {
            return getPiece(position).getTeamColor();
        }
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //make new board
        //put pieces in right spot

        //Add all white pieces
        boardGrid = new ChessPiece[8][8];

        // Place Pawns
        for (int i = 0; i < 8; i++) {
            boardGrid[1][i] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            boardGrid[6][i] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }

        // Piece order for the first and last row
        ChessPiece.PieceType[] order = {
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KING, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK
        };

        // Place other pieces
        for (int i = 0; i < 8; i++) {
            boardGrid[0][i] = new ChessPiece(ChessGame.TeamColor.WHITE, order[i]); // White pieces
            boardGrid[7][i] = new ChessPiece(ChessGame.TeamColor.BLACK, order[i]); // Black pieces
        }

    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int row = 7; row >= 0; row--) {  // Start from top row (8th row)
            output.append("|");
            for (int col = 0; col < 8; col++) {
                if (boardGrid[row][col] != null) {
                    output.append(boardGrid[row][col].getTeamColor());
                } else {
                    output.append("  ");
                }
                output.append("|");
            }
            output.append("\n");
        }
        return output.toString();
    }


    @Override
    public int hashCode() {
        return Arrays.deepHashCode(boardGrid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Arrays.deepEquals(boardGrid, that.boardGrid);
    }
}
