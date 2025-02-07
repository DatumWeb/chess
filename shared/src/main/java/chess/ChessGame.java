package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor whosTurn;
    private ChessBoard gameBoard;

    public ChessGame() {
        gameBoard = new ChessBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return whosTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        whosTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    public ChessBoard makeSimulationBoard () {
        ChessBoard simulationBoard = new ChessBoard();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = gameBoard.getPiece(position);

                if (piece != null) {
                    simulationBoard.addPiece(position, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                }
            }
        }
        return simulationBoard;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        HashSet<ChessMove> validMoves = new HashSet<>();

        ChessPiece pieceToMove = gameBoard.getPiece(startPosition);
        if (pieceToMove == null || pieceToMove.getTeamColor() != whosTurn) {
            return null;
        }


        Collection<ChessMove> movesToCheck = pieceToMove.pieceMoves(gameBoard, startPosition);


        for (ChessMove move : movesToCheck) {
            ChessBoard simulationBoard = makeSimulationBoard();

            simulationBoard.addPiece(move.getEndPosition(), simulationBoard.getPiece(move.getStartPosition()));
            simulationBoard.addPiece(move.getStartPosition(), null);

            ChessGame simulationGame = new ChessGame();
            simulationGame.setBoard(simulationBoard);
            simulationGame.setTeamTurn(whosTurn); // Ensure turn matches simulation

            if (!simulationGame.isInCheck(whosTurn)) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece pieceToMove = gameBoard.getPiece(move.getStartPosition());

        if (pieceToMove == null || pieceToMove.getTeamColor() != whosTurn) {
            throw new InvalidMoveException("That isn't your piece");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        if(validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException("This move is invalid");
        }

        ChessPiece pieceToCapture = gameBoard.getPiece(move.getEndPosition()); //I think I might need to make this null after this
        gameBoard.addPiece(move.getEndPosition(), pieceToMove);
        gameBoard.addPiece(move.getStartPosition(), null); //should I make a remove piece function

        TeamColor opposingTeam = (whosTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        whosTurn = (whosTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        //if teamColor is in check return true
        //else return false
        //find the king
        //find if any piece on the other team can make a move to the king
        ChessPosition kingPosition = null;
        for(int row = 1; row <= 8; row++) {
            for(int col = 1; col <= 8; col++){
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece pieceAt = gameBoard.getPiece(currentPosition);

                if (pieceAt != null && pieceAt.getPieceType() == ChessPiece.PieceType.KING && pieceAt.getTeamColor() ==teamColor) {
                    kingPosition = currentPosition;
                    break;
                }
            }
            if (kingPosition != null) break;

        }

        //need to place
        TeamColor opposingTeam = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece piece = gameBoard.getPiece(currentPosition);

                if (piece != null && piece.getTeamColor() == opposingTeam) {
                    Collection<ChessMove> possibleMoves = piece.pieceMoves(gameBoard, currentPosition);

                    for (ChessMove move : possibleMoves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return  false;


    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        //if teamColor is in checkmate return true
        //else return false
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        //if no valid moves return True
        //otherwise return false
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.gameBoard = gameBoard;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }
}
