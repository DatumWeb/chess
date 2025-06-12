package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.DAOFactory;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private static final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Session>> GAME_SESSIONS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, String> SESSION_TO_AUTH = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, Integer> SESSION_TO_GAME = new ConcurrentHashMap<>();

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;
    private final Gson gson = new Gson();

    public WebSocketHandler() throws Exception {
        this.authDAO = DAOFactory.createAuthDAO();
        this.gameDAO = DAOFactory.createGameDAO();
        this.userDAO = DAOFactory.createUserDAO();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + reason);
        cleanupSession(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT:
                    handleConnect(session, command);
                    break;
                case MAKE_MOVE:
                    handleMakeMove(session, message);
                    break;
                case LEAVE:
                    handleLeave(session, command);
                    break;
                case RESIGN:
                    handleResign(session, command);
                    break;
                default:
                    sendError(session, "Unsupported command type");
            }
        } catch (Exception e) {
            sendError(session, "Error processing command: " + e.getMessage());
        }
    }

    private void handleConnect(Session session, UserGameCommand command) {
        try {
            var authData = authDAO.getAuthToken(command.getAuthToken());
            if (authData == null) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            SESSION_TO_AUTH.put(session, command.getAuthToken());
            SESSION_TO_GAME.put(session, command.getGameID());
            GAME_SESSIONS.computeIfAbsent(command.getGameID(), k -> new ConcurrentHashMap<>())
                    .put(command.getAuthToken(), session);

            sendToSession(session, new LoadGameMessage(gameData.game()));

            String username = authData.username();
            String notificationMsg = username + " joined the game.";
            sendNotificationToOthers(command.getGameID(), command.getAuthToken(), notificationMsg);

            System.out.println("User " + username + " connected to game " + command.getGameID());

        } catch (Exception e) {
            sendError(session, "Error connecting to game: " + e.getMessage());
        }
    }

    private void handleMakeMove(Session session, String message) {
        try {
            MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);

            var authData = authDAO.getAuthToken(moveCommand.getAuthToken());
            if (authData == null) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            GameData gameData = gameDAO.getGame(moveCommand.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            ChessMove move = moveCommand.getMove();
            ChessGame game = gameData.game();

            if (game.isGameOver()) {
                sendError(session, "Game is already over. No moves allowed.");
                return;
            }


            String username = authData.username();
            ChessGame.TeamColor expectedTeam = game.getTeamTurn();

            if ((expectedTeam == ChessGame.TeamColor.WHITE && !username.equals(gameData.whiteUsername())) ||
                    (expectedTeam == ChessGame.TeamColor.BLACK && !username.equals(gameData.blackUsername()))) {
                sendError(session, "Error: Not your turn!");
                return;
            }


            try {
                game.makeMove(move);
            } catch (InvalidMoveException e) {
                sendError(session, "Error: " + e.getMessage());
                return;
            }

            gameDAO.updateGame(gameData);

            String moveStr = formatMove(move);
            sendNotificationToOthers(moveCommand.getGameID(), moveCommand.getAuthToken(), authData.username() + " moved: " + moveStr);

            sendGameStateToAll(moveCommand.getGameID(), game);

            checkGameState(moveCommand.getGameID(), game);

        } catch (Exception e) {
            sendError(session, "Error processing move: " + e.getMessage());
        }
    }

    private void handleResign(Session session, UserGameCommand command) {
        try {
            var authData = authDAO.getAuthToken(command.getAuthToken());
            if (authData == null) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            ChessGame game = gameData.game();

            if (game.isGameOver()) {
                sendError(session, "Error: Game is already over. No further resignations allowed.");
                return;
            }

            String username = authData.username();
            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                sendError(session, "Error: Only players can resign.");
                return;
            }


            game.setGameOver(true);
            gameDAO.updateGame(gameData);

            sendNotificationToAll(command.getGameID(), username + " has resigned. Game over.");
            sendGameStateToAll(command.getGameID(), game);

        } catch (Exception e) {
            sendError(session, "Error resigning game: " + e.getMessage());
        }
    }

    private void handleLeave(Session session, UserGameCommand command) {
        try {
            var authData = authDAO.getAuthToken(command.getAuthToken());
            if (authData == null) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            Integer gameID = command.getGameID();
            String username = authData.username();

            var sessions = GAME_SESSIONS.get(gameID);
            if (sessions != null) {
                sessions.remove(command.getAuthToken());
                if (sessions.isEmpty()) {
                    GAME_SESSIONS.remove(gameID);
                }
            }

            GameData gameData = gameDAO.getGame(gameID);
            if (gameData != null) {
                if (username.equals(gameData.whiteUsername())) {
                    gameDAO.updateGame(new GameData(gameID, null, gameData.blackUsername(), gameData.gameName(), gameData.game()));
                } else if (username.equals(gameData.blackUsername())) {
                    gameDAO.updateGame(new GameData(gameID, gameData.whiteUsername(), null, gameData.gameName(), gameData.game()));
                }
            }

            sendNotificationToOthers(gameID, command.getAuthToken(), username + " has left the game.");

            System.out.println("User " + username + " left game " + gameID);

        } catch (Exception e) {
            sendError(session, "Error leaving game: " + e.getMessage());
        }
    }

    private String formatMove(ChessMove move) {
        return String.format("%s to %s",
                positionToString(move.getStartPosition()),
                positionToString(move.getEndPosition()));
    }

    private String positionToString(ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        return "" + col + pos.getRow();
    }

    private void sendNotificationToAll(Integer gameID, String notificationMessage) {
        var sessions = GAME_SESSIONS.get(gameID);
        if (sessions != null) {
            sessions.values().forEach(session -> sendToSession(session, new NotificationMessage(notificationMessage)));
        }
    }

    private void sendGameStateToAll(Integer gameID, ChessGame game) {
        var sessions = GAME_SESSIONS.get(gameID);
        if (sessions != null) {
            sessions.values().forEach(session -> sendToSession(session, new LoadGameMessage(game)));
        }
    }

    private void checkGameState(Integer gameID, ChessGame game) {
        try {
            ChessGame.TeamColor currentTeam = game.getTeamTurn();

            if (game.isInCheckmate(currentTeam)) {
                String teamName = (currentTeam == ChessGame.TeamColor.WHITE) ? "White" : "Black";
                sendNotificationToAll(gameID, teamName + " is in checkmate");
                game.setGameOver(true);
                gameDAO.updateGame(new GameData(gameID, gameDataWhite(gameID), gameDataBlack(gameID), gameDataName(gameID), game));
            } else if (game.isInStalemate(currentTeam)) {
                sendNotificationToAll(gameID, "Game ended in stalemate");
                game.setGameOver(true);
                gameDAO.updateGame(new GameData(gameID, gameDataWhite(gameID), gameDataBlack(gameID), gameDataName(gameID), game));
            } else if (game.isInCheck(currentTeam)) {
                String teamName = (currentTeam == ChessGame.TeamColor.WHITE) ? "White" : "Black";
                sendNotificationToAll(gameID, teamName + " is in check");
            }
        } catch (Exception e) {
            System.err.println("Error checking game state: " + e.getMessage());
        }
    }

    private String gameDataWhite(Integer gameID) {
        try {
            GameData gameData = gameDAO.getGame(gameID);
            return gameData.whiteUsername();
        } catch (Exception e) {
            System.err.println("Error retrieving white username for game " + gameID + ": " + e.getMessage());
            return "";
        }
    }

    private String gameDataBlack(Integer gameID) {
        try {
            GameData gameData = gameDAO.getGame(gameID);
            return gameData.blackUsername();
        } catch (Exception e) {
            System.err.println("Error retrieving black username for game " + gameID + ": " + e.getMessage());
            return "";
        }
    }

    private String gameDataName(Integer gameID) {
        try {
            GameData gameData = gameDAO.getGame(gameID);
            return gameData.gameName();
        } catch (Exception e) {
            System.err.println("Error retrieving game name for game " + gameID + ": " + e.getMessage());
            return "";
        }
    }


    private void sendNotificationToOthers(Integer gameID, String excludeAuthToken, String notificationMessage) {
        var sessions = GAME_SESSIONS.get(gameID);
        if (sessions != null) {
            sessions.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(excludeAuthToken))
                    .forEach(entry -> sendToSession(entry.getValue(), new NotificationMessage(notificationMessage)));
        }
    }

    private void cleanupSession(Session session) {
        String authToken = SESSION_TO_AUTH.remove(session);
        Integer gameID = SESSION_TO_GAME.remove(session);
        if (authToken != null && gameID != null) {
            var sessions = GAME_SESSIONS.get(gameID);
            if (sessions != null) {
                sessions.remove(authToken);
                if (sessions.isEmpty()) {
                    GAME_SESSIONS.remove(gameID);
                }
            }
        }
    }

    private void sendError(Session session, String errorMessage) {
        sendToSession(session, new ErrorMessage(errorMessage));
    }

    private void sendToSession(Session session, Object message) {
        if (session.isOpen()) {
            try {
                session.getRemote().sendString(gson.toJson(message));
            } catch (IOException e) {
                System.err.println("Error sending message to session: " + e.getMessage());
            }
        }
    }
}