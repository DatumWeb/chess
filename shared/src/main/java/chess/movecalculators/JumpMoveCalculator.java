package chess.movecalculators;

import chess.*;

import java.util.Set;
import java.util.HashSet;

public abstract class JumpMoveCalculator implements PieceMoveCalculators {
    protected Set<ChessMove> getJumpMoves(ChessBoard board, ChessPosition activePosition, int[][] moveSet) {
        Set<ChessMove> moves = new HashSet<>();
        int activeX = activePosition.getColumn();
        int activeY = activePosition.getRow();
        ChessGame.TeamColor team = board.getTeamOnSquare(activePosition);

        // Iterate over each potential move offset
        for (int[] offset : moveSet) {
            ChessPosition nextPosition = new ChessPosition(activeY + offset[1], activeX + offset[0]);

            // Check if the next position is valid and the move is legal
            if (isBoarded(nextPosition)) {
                if (board.getPiece(nextPosition) == null || board.getTeamOnSquare(nextPosition) != team) {
                    moves.add(new ChessMove(activePosition, nextPosition, null));
                }
            }
        }

        return moves;
    }

    // Check if the position is within the board boundaries
    public boolean isBoarded(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }
}