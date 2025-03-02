package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Set;

public interface PieceMoveCalculators {

    Set<ChessMove> getMoves(ChessBoard board, ChessPosition activePosition);

    default boolean isBoarded(ChessPosition position) {
        //if the position is on the board then return true, else return false
        //checks if it is off the row or off the column
        return ((position.getRow() <= 8) && (position.getRow() >= 1)) && ((position.getColumn() <= 8) && (position.getColumn() >= 1));
    }
}