package chess.piececalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.HashSet;
import java.util.Set;

public abstract class SteppingMoveCalculator implements PieceMoveCalculator {
    protected abstract int[][] getPotentialMoves();
    @Override
    public Set<ChessMove> getMoves(ChessBoard gameBoard, ChessPosition activePosition) {
        Set<ChessMove> moves = new HashSet<>();

        int[][] potentialMoves = getPotentialMoves();

        for (int[] jump : potentialMoves) {
            int step = 1;

            while (true) {
                int testRow = activePosition.getRow() + jump[1] * step;
                int testCol = activePosition.getColumn() + jump[0] * step;

                ChessPosition testPosition = new ChessPosition(testRow, testCol);
                if (!isRealPosition(testPosition)) {
                    break;
                }

                if (gameBoard.getPiece(testPosition) == null) {
                    moves.add(new ChessMove(activePosition, testPosition, null));
                    step++;
                } else {
                    if (gameBoard.getColorOnSquare(testPosition) != gameBoard.getColorOnSquare(activePosition)) {
                        moves.add(new ChessMove(activePosition, testPosition, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }
}