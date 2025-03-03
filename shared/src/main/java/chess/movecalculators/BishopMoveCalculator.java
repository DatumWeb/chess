package chess.movecalculators;

import chess.*;
import chess.ChessMove;

import java.util.Set;
import java.util.HashSet;

public class BishopMoveCalculator extends CrossMoveCalculator {
    @Override
    public Set<ChessMove> getMoves(ChessBoard board, ChessPosition activePosition) {
        Set<ChessMove> moves = new HashSet<>();
        int[][] moveDirections = {{-1, 1}, {1, 1}, {1, -1}, {-1, -1}};
        return getCrossMoves(board, activePosition, moveDirections);
    }

}
