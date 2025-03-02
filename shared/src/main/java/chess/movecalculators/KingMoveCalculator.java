package chess.movecalculators;

import chess.*;
import chess.ChessMove;

import java.util.Set;
import java.util.HashSet;

public class KingMoveCalculator extends JumpMoveCalculator {
    @Override
    public Set<ChessMove> getMoves(ChessBoard board, ChessPosition activePosition) {
        Set<ChessMove> moves = new HashSet<>();
        int[][] moveOffsets = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };

        return getJumpMoves(board, activePosition, moveOffsets);
    }
}
