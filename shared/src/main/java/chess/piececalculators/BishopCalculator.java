package chess.piececalculators;
public class BishopCalculator extends SteppingMoveCalculator {
    @Override
    protected int[][] getPotentialMoves() {
        return new int[][]{{1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
    }
}
