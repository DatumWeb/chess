package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece [][] gameBoardGrid;
    public ChessBoard() {
        gameBoardGrid = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        gameBoardGrid[position.getRow() -1] [position.getColumn() -1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return gameBoardGrid[position.getRow() - 1][position.getColumn() - 1];
    }

    public ChessGame.TeamColor getColorOnSquare (ChessPosition positionToCheck) {
        ChessGame.TeamColor colorOnSquare = null;
        if (getPiece(positionToCheck) != null) {
            colorOnSquare = getPiece(positionToCheck).getTeamColor();
        }
        return colorOnSquare;
    }
    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        throw new RuntimeException("Not implemented");
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
        return Arrays.equals(gameBoardGrid, that.gameBoardGrid);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(gameBoardGrid);
    }
}
