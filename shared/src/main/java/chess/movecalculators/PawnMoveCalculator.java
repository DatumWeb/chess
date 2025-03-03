package chess.movecalculators;

import chess.*;

import java.util.Set;
import java.util.HashSet;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

public class PawnMoveCalculator implements PieceMoveCalculators {
    @Override
    public Set<ChessMove> getMoves(ChessBoard board, ChessPosition currentPosition) {
        Set<ChessMove> moves = new HashSet<>();
        int currentX = currentPosition.getColumn();
        int currentY = currentPosition.getRow();
        ChessGame.TeamColor teamColor = board.getTeamOnSquare(currentPosition);

        int moveDirection = (teamColor == WHITE) ? 1 : -1;
        boolean isPromotionRow = (teamColor == WHITE && currentY == 7) || (teamColor == BLACK && currentY == 2);
        ChessPiece.PieceType[] promotionOptions = isPromotionRow
                ? new ChessPiece.PieceType[]
                {ChessPiece.PieceType.ROOK, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KNIGHT}
                : new ChessPiece.PieceType[]{null};

        for (ChessPiece.PieceType promotionPiece : promotionOptions) {
            // One-step forward
            ChessPosition oneStep = new ChessPosition(currentY + moveDirection, currentX);
            if (isBoarded(oneStep) && board.getPiece(oneStep) == null) {
                moves.add(new ChessMove(currentPosition, oneStep, promotionPiece));

                // Two-step forward
                ChessPosition twoStep = new ChessPosition(currentY + moveDirection * 2, currentX);
                if ((teamColor == WHITE && currentY == 2) || (teamColor == BLACK && currentY == 7)) {
                    if (isBoarded(twoStep) && board.getPiece(twoStep) == null) {
                        moves.add(new ChessMove(currentPosition, twoStep, promotionPiece));
                    }
                }
            }

            // Capture moves
            ChessPosition leftAttack = new ChessPosition(currentY + moveDirection, currentX - 1);
            if (isBoarded(leftAttack) && board.getPiece(leftAttack) != null && teamColor != board.getTeamOnSquare(leftAttack)) {
                moves.add(new ChessMove(currentPosition, leftAttack, promotionPiece));
            }

            ChessPosition rightAttack = new ChessPosition(currentY + moveDirection, currentX + 1);
            if (isBoarded(rightAttack) && board.getPiece(rightAttack) != null && teamColor != board.getTeamOnSquare(rightAttack)) {
                moves.add(new ChessMove(currentPosition, rightAttack, promotionPiece));
            }
        }

        return moves;
    }
}
