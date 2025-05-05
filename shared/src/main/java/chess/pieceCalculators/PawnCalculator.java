package chess.pieceCalculators;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.HashSet;
import java.util.Set;

public class PawnCalculator implements pieceMoveCalculator {
    public Set<ChessMove> getMoves(ChessBoard gameBoard, ChessPosition activePosition){
        Set<ChessMove> moves = new HashSet<>();

        ChessGame.TeamColor activeColor = gameBoard.getColorOnSquare(activePosition);

        int stepDirection = 0;
        if (activeColor == ChessGame.TeamColor.WHITE) {
            stepDirection = 1;
        } else {
            stepDirection = -1; //if black
        }

        //call the move helper functions
        getForwardMoves(gameBoard, moves, activePosition, stepDirection, activeColor);

        getAttackMoves (gameBoard, moves, activePosition, stepDirection, activeColor);

        return moves;
    }

    public void getForwardMoves (ChessBoard gameBoard, Set<ChessMove> moves, ChessPosition activePosition, int stepDirection, ChessGame.TeamColor activeColor) {
        int testRow = activePosition.getRow() + stepDirection;
        int testCol = activePosition.getColumn();

        ChessPosition testPositionOneStep = new ChessPosition(testRow, testCol);
        ChessPosition testPositionTwoStep = new ChessPosition(testRow * 2, testCol);


        if (isStartingTurn(activePosition, activeColor)){
            if (gameBoard.getPiece(testPositionOneStep) == null && isRealPosition(testPositionOneStep)){
                moves.add(new ChessMove(activePosition, testPositionOneStep, null));

                if(gameBoard.getPiece(testPositionTwoStep) == null && isRealPosition(testPositionOneStep)){
                    moves.add(new ChessMove(activePosition, testPositionTwoStep, null));
                }
            }
        } else {
            if (gameBoard.getPiece(testPositionOneStep) == null && isRealPosition(testPositionOneStep)) {
                moves.add(new ChessMove(activePosition, testPositionOneStep, null));
            }
        }

    }

    public void getAttackMoves (ChessBoard gameBoard, Set<ChessMove> moves, ChessPosition activePosition, int stepDirection, ChessGame.TeamColor activeColor) {
        int testRow = activePosition.getRow() + stepDirection;
        int testColRight = activePosition.getColumn() + 1;
        int testColLeft = activePosition.getColumn() - 1;

        ChessPosition testLeft = new ChessPosition(testRow, testColLeft);
        ChessPosition testRight = new ChessPosition(testRow, testColRight);

        if (gameBoard.getColorOnSquare(testRight) != activeColor && isRealPosition(testRight) && gameBoard.getPiece(testRight) != null){
            moves.add(new ChessMove(activePosition, testRight, null));
        }
        if (gameBoard.getColorOnSquare(testLeft) != activeColor && isRealPosition(testLeft) && gameBoard.getPiece(testLeft) != null) {
            moves.add(new ChessMove(activePosition, testLeft, null));
        }
    }

    public boolean isPromotionRow (ChessPosition testPosition) {
        return testPosition.getRow() == 1 || testPosition.getRow() == 8;
    }

    public boolean isStartingTurn (ChessPosition activePosition, ChessGame.TeamColor activeColor) {
        return (activePosition.getColumn() == 2 && activeColor == ChessGame.TeamColor.WHITE) || (activePosition.getColumn() == 7 && activeColor == ChessGame.TeamColor.BLACK);
    }
}
