package chess.piececalculators;

public class KingCalculator extends JumpingMoveCalculator {
    @Override
    protected int[][] getPotentialMoves() {
        return new int[][]{
                {1, 0}, {1, 1}, {0, 1}, {-1, 0}, {-1, -1}, {0, -1}, {-1, 1}, {1, -1}
        };
    }
}