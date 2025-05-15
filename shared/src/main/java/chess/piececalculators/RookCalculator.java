package chess.piececalculators;

public class RookCalculator extends SteppingMoveCalculator {
    @Override
    protected int[][] getPotentialMoves() {
        return new int[][] {{1,0}, {-1, 0}, {0, 1}, {0, -1}};
    }
}
