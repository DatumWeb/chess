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

    private TeamColor activeTeamColor;
    private ChessBoard gameBoard;

    public ChessGame() {
        this.activeTeamColor = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return activeTeamColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        activeTeamColor = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return activeTeamColor == chessGame.activeTeamColor && Objects.equals(gameBoard, chessGame.gameBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeTeamColor, gameBoard);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    private void simulateMove(ChessPosition startPosition, ChessMove move) {
        ChessPiece piece = gameBoard.getPiece(startPosition);
        gameBoard.addPiece(startPosition, null);
        gameBoard.addPiece(move.getEndPosition(), piece);
    }

    private void undoMove(ChessPosition startPosistion, ChessMove move, ChessPiece capturedPiece) {
        ChessPiece piece = gameBoard.getPiece(move.getEndPosition());
        gameBoard.addPiece(move.getEndPosition(), capturedPiece);
        gameBoard.addPiece(startPosistion, piece);
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

    private void switchTurn() {
        activeTeamColor = (activeTeamColor == TeamColor.WHITE) ? TeamColor.BLACK: TeamColor.WHITE;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        if (validMoves == null || !validMoves.contains(move) || (gameBoard.getColorOnSquare(move.getStartPosition()) != getTeamTurn())) {
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
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
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
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        throw new RuntimeException("Not implemented");
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
