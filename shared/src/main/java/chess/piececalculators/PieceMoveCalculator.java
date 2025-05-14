package chess.piececalculators;

import chess.*;

import java.util.Set;
public interface PieceMoveCalculator {

    Set<ChessMove> getMoves(ChessBoard gameBoard, ChessPosition activePosition);

    default boolean isRealPosition(ChessPosition position) {
        return position.getRow() <= 8 && position.getRow() >= 1 && position.getColumn() <= 8 && position.getColumn() >= 1;
    }
}