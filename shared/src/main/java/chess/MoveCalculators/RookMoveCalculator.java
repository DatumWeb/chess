package chess.MoveCalculators;

import chess.*;
import chess.ChessMove;

import java.util.Set;
import java.util.HashSet;

public class RookMoveCalculator implements PieceMoveCalculators {
    @Override
    public Set<ChessMove> getMoves(ChessBoard board, ChessPosition activePosition) {
        Set<ChessMove> moves = new HashSet<>();

        // Rooks move vertically or horizontally (straight lines)
        int[][] moveDirections = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1} // Vertical and horizontal directions
        };

        int activeX = activePosition.getColumn();
        int activeY = activePosition.getRow();
        ChessGame.TeamColor team = board.getTeamOnSquare(activePosition);

        for (int[] direction : moveDirections) {
            int step = 1;

            while (true) {
                ChessPosition nextPosition = new ChessPosition(activeY + direction[1] * step, activeX + direction[0] * step);

                if (!isBoarded(nextPosition)) break;

                if (board.getPiece(nextPosition) == null) {
                    moves.add(new ChessMove(activePosition, nextPosition, null));
                } else {
                    if (board.getTeamOnSquare(nextPosition) != team) {
                        moves.add(new ChessMove(activePosition, nextPosition, null));
                    }
                    break;
                }
                step++;
            }
        }

        return moves;
    }
}
