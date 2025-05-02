package chess.pieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.HashSet;
import java.util.Set;

public class RookCalculator implements pieceMoveCalculator{
    @Override
    public Set<ChessMove> getMoves(ChessBoard gameBoard, ChessPosition activePosition) {
        Set<ChessMove> moves = new HashSet<>();

        int [][] potentialMoves = {{1,0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] jump : potentialMoves) {
            int step = 0;

            while(true){

                int testRow = activePosition.getRow() + jump[1] + step;
                int testCol = activePosition.getColumn() + jump[0] + step;
                ChessPosition testPosition = new ChessPosition(testRow, testCol);
                if (isRealPosition(testPosition)){
                    moves.add(new ChessMove(activePosition, testPosition, null));
                    step++;
                } else {
                    break;
                }

            }
        }
        return moves;
    }
}
