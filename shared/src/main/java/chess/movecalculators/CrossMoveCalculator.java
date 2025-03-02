package chess.movecalculators;

import chess.*;

import java.util.Set;
import java.util.HashSet;

public abstract class CrossMoveCalculator implements PieceMoveCalculators {
    protected Set<ChessMove> getCrossMoves(ChessBoard board, ChessPosition activePosition, int[][] moveDirections) {
        Set<ChessMove> moves = new HashSet<>();
        int activeX = activePosition.getColumn();
        int activeY = activePosition.getRow();
        ChessGame.TeamColor team = board.getTeamOnSquare(activePosition);

        for (int[] direction : moveDirections) {
            int step = 1;

            while (true) {
                ChessPosition nextPosition = new ChessPosition(activeY + direction[1] * step, activeX + direction[0] * step);

                if (!isBoarded(nextPosition)) {
                    break;
                }

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

    public boolean isBoarded(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }
}
