package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor whosTurn;
    private ChessBoard gameBoard;
    //private boolean gameOver;

    public ChessGame() {
        this.gameBoard = new ChessBoard();
        this.whosTurn = TeamColor.WHITE;
        //this.gameOver = false;
    }

    /**
     * @return The team whose turn it is.
     */
    public TeamColor getTeamTurn() {
        return whosTurn;
    }

    /**
     * Set's which team's turn it is.
     *
     * @param team The team whose turn it is.
     */
    public void setTeamTurn(TeamColor team) {
        this.whosTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game.
     */
    public enum TeamColor {
        WHITE,
        BLACK;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece startPiece = gameBoard.getPiece(startPosition);
        if (startPiece == null) {
            return null;
        }

        Collection<ChessMove> movesToTry = startPiece.pieceMoves(gameBoard, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();

        for (ChessMove move : movesToTry) {
            ChessPiece tempPiece = gameBoard.getPiece(move.getEndPosition());
            simulateMove(startPosition, move);

            if (!isInCheck(startPiece.getTeamColor())) {
                validMoves.add(move);
            }

            undoMove(startPosition, move, tempPiece);
        }

        return validMoves;
    }

    private void undoMove(ChessPosition start, ChessMove move, ChessPiece capturedPiece) {
        ChessPiece piece = gameBoard.getPiece(move.getEndPosition());
        gameBoard.addPiece(move.getEndPosition(), capturedPiece);
        gameBoard.addPiece(start, piece);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        if (validMoves == null || !validMoves.contains(move) || (gameBoard.getTeamOnSquare(move.getStartPosition()) != getTeamTurn())) {
            throw new InvalidMoveException("Invalid move");
        }

        ChessPiece pieceToMove = gameBoard.getPiece(move.getStartPosition());
        if (move.getPromotionPiece() != null) {
            pieceToMove = new ChessPiece(pieceToMove.getTeamColor(), move.getPromotionPiece());
        }

        gameBoard.addPiece(move.getStartPosition(), null);
        gameBoard.addPiece(move.getEndPosition(), pieceToMove);
        switchTurn();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = gameBoard.getPiece(currentPosition);

                if (currentPiece != null && currentPiece.getTeamColor() != teamColor) {
                    for (ChessMove move : currentPiece.pieceMoves(gameBoard, currentPosition)) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = gameBoard.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        throw new IllegalStateException("King not found on board.");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = gameBoard.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);

                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = gameBoard.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }

    //public void setGameOver(boolean gameOver) {
    //    this.gameOver = gameOver;
    //}

    //public boolean isGameOver() {
    //    return gameOver;
    //}

    private void switchTurn() {
        whosTurn = (whosTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }



    private void simulateMove(ChessPosition start, ChessMove move) {
        ChessPiece piece = gameBoard.getPiece(start);
        gameBoard.addPiece(start, null);
        gameBoard.addPiece(move.getEndPosition(), piece);
    }


    @Override
    public String toString() {
        return "ChessGame{" +
                "teamTurn=" + whosTurn +
                ", board=" + gameBoard +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(whosTurn, chessGame.whosTurn) && Objects.equals(gameBoard, chessGame.gameBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(whosTurn, gameBoard);
    }
}
