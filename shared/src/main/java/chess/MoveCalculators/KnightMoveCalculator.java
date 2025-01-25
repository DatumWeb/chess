package chess.MoveCalculators;

import chess.*;
import chess.ChessMove;

import java.util.Set;
import java.util.HashSet;

public class KnightMoveCalculator implements PieceMoveCalculators {
    @Override
    public Set<ChessMove> getMoves(ChessBoard board, ChessPosition activePosition) {
        Set<ChessMove> moves = new HashSet<>();
        int[][] moveOffsets = {
                {-2, -1}, {-2, 1}, {2, -1}, {2, 1},
                {-1, -2}, {1, -2}, {-1, 2}, {1, 2}
        };

        int activeX = activePosition.getColumn();
        int activeY = activePosition.getRow();
        ChessGame.TeamColor team = board.getTeamOnSquare(activePosition);

        for (int[] offset : moveOffsets) {
            ChessPosition nextPosition = new ChessPosition(activeY + offset[1], activeX + offset[0]);

            if (!isBoarded(nextPosition)) continue;

            if (board.getPiece(nextPosition) == null || board.getTeamOnSquare(nextPosition) != team) {
                moves.add(new ChessMove(activePosition, nextPosition, null));
            }
        }

        return moves;
    }
}
