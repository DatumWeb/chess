package chess.piececalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.HashSet;
import java.util.Set;

public class QueenCalculator extends SteppingMoveCalculator {
    @Override
    protected int[][] getPotentialMoves() {
        return new int[][]{{0, 1}, {1, 0}, {1, 1}, {0, -1}, {-1, 0}, {1, -1}, {-1, 1}, {-1, -1}};
    }
}