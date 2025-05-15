package chess.piececalculators;

public class QueenCalculator extends SteppingMoveCalculator {
    @Override
    protected int[][] getPotentialMoves() {
        return new int[][]{{0, 1}, {1, 0}, {1, 1}, {0, -1}, {-1, 0}, {1, -1}, {-1, 1}, {-1, -1}};
    }
}