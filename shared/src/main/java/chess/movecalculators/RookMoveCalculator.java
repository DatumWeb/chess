package chess.movecalculators;

import chess.*;
import chess.ChessMove;

import java.util.Set;

public class RookMoveCalculator extends CrossMoveCalculator {
    @Override
    public Set<ChessMove> getMoves(ChessBoard board, ChessPosition activePosition) {
        int[][] moveDirections = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}
        };
        return getCrossMoves(board, activePosition, moveDirections);
    }
}
