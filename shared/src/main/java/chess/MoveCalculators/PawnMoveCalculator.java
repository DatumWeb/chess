package chess.MoveCalculators;

import chess.*;

import java.util.Set;
import java.util.HashSet;

public class PawnMoveCalculator implements PieceMoveCalculators {

    @Override
    public Set<ChessMove> getMoves(ChessBoard board, ChessPosition currPosition) {
        HashSet<ChessMove> moves = new HashSet<>(16); //16 is the max number of moves of a Pawn
        int currX = currPosition.getColumn();
        int currY = currPosition.getRow();
        ChessPiece.PieceType[] promotionPieces = new ChessPiece.PieceType[]{null};

        ChessGame.TeamColor team = board.getTeamOnSquare(currPosition);
        int moveIncrement = team == ChessGame.TeamColor.WHITE ? 1 : -1;

        // Check for promotion
        boolean promote = (team == ChessGame.TeamColor.WHITE && currY == 7) || (team == ChessGame.TeamColor.BLACK && currY == 2);
        if (promote) {
            promotionPieces = new ChessPiece.PieceType[]{ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.QUEEN};
        }

        for (ChessPiece.PieceType promotionPiece : promotionPieces) {

            ChessPosition forwardPosition = new ChessPosition(currY + moveIncrement, currX);
            if (isBoarded(forwardPosition) && board.getPiece(forwardPosition) == null) {
                moves.add(new ChessMove(currPosition, forwardPosition, promotionPiece));
            }


            ChessPosition leftAttack = new ChessPosition(currY + moveIncrement, currX - 1);
            if (isBoarded(leftAttack) &&
                    board.getPiece(leftAttack) != null &&
                    board.getTeamOnSquare(leftAttack) != team) {
                moves.add(new ChessMove(currPosition, leftAttack, promotionPiece));
            }


            ChessPosition rightAttack = new ChessPosition(currY + moveIncrement, currX + 1);
            if (isBoarded(rightAttack) &&
                    board.getPiece(rightAttack) != null &&
                    board.getTeamOnSquare(rightAttack) != team) {
                moves.add(new ChessMove(currPosition, rightAttack, promotionPiece));
            }

            // Add first move double
            ChessPosition doubleForwardPosition = new ChessPosition(currY + moveIncrement * 2, currX);
            if (isBoarded(doubleForwardPosition) &&
                    ((team == ChessGame.TeamColor.WHITE && currY == 2) || (team == ChessGame.TeamColor.BLACK && currY == 7)) &&
                    board.getPiece(doubleForwardPosition) == null &&
                    board.getPiece(forwardPosition) == null) {
                moves.add(new ChessMove(currPosition, doubleForwardPosition, promotionPiece));
            }
        }

        return moves;
    }
}
