package chess.piececalculators;

public class KnightCalculator extends JumpingMoveCalculator {
    @Override
    protected int[][] getPotentialMoves() {
        return new int[][]{
                {2, -1}, {2, 1}, {1, -2}, {1, 2}, {-2, 1}, {-2, -1}, {-1, 2}, {-1, -2}
        };
    }
}