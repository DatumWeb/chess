package chess.movecalculators;

import chess.*;
import chess.ChessMove;

import java.util.Set;
import java.util.HashSet;

public class KnightMoveCalculator extends JumpMoveCalculator {
    @Override
    public Set<ChessMove> getMoves(ChessBoard board, ChessPosition activePosition) {
        Set<ChessMove> moves = new HashSet<>();
        int[][] moveOffsets = {
                {-2, -1}, {-2, 1}, {2, -1}, {2, 1},
                {-1, -2}, {1, -2}, {-1, 2}, {1, 2}
        };

        return getJumpMoves(board, activePosition, moveOffsets);
    }
}
