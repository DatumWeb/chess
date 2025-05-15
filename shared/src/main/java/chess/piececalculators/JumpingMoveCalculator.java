package chess.piececalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.HashSet;
import java.util.Set;

public abstract class JumpingMoveCalculator implements PieceMoveCalculator {

    protected abstract int[][] getPotentialMoves();
    @Override
    public Set<ChessMove> getMoves(ChessBoard gameBoard, ChessPosition activePosition) {
        Set<ChessMove> moves = new HashSet<>();

        int[][] potentialMoves = getPotentialMoves();

        for (int[] jump : potentialMoves) {
            int testRow = activePosition.getRow() + jump[1];
            int testCol = activePosition.getColumn() + jump[0];

            ChessPosition testPosition = new ChessPosition(testRow, testCol);

            if (isRealPosition(testPosition)) {
                if (gameBoard.getPiece(testPosition) == null) {
                    moves.add(new ChessMove(activePosition, testPosition, null));
                } else if (gameBoard.getColorOnSquare(testPosition) != gameBoard.getColorOnSquare(activePosition)) {
                    moves.add(new ChessMove(activePosition, testPosition, null));
                }
            }
        }
        return moves;
    }
}