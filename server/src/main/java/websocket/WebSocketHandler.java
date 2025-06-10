package websocket;

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
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private static final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Session>> gameSessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, String> sessionToAuth = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, Integer> sessionToGame = new ConcurrentHashMap<>();

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
            if (command.getCommandType().equals(UserGameCommand.CommandType.CONNECT)) {
                handleConnect(session, command);
            } else {
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

            sessionToAuth.put(session, command.getAuthToken());
            sessionToGame.put(session, command.getGameID());
            gameSessions.computeIfAbsent(command.getGameID(), k -> new ConcurrentHashMap<>())
                    .put(command.getAuthToken(), session);

            sendToSession(session, new LoadGameMessage(gameData.game()));

            System.out.println("User " + authData.username() + " connected to game " + command.getGameID());
        } catch (Exception e) {
            sendError(session, "Error connecting to game: " + e.getMessage());
        }
    }

    private void cleanupSession(Session session) {
        String authToken = sessionToAuth.remove(session);
        Integer gameID = sessionToGame.remove(session);
        if (authToken != null && gameID != null) {
            var sessions = gameSessions.get(gameID);
            if (sessions != null) {
                sessions.remove(authToken);
                if (sessions.isEmpty()) {
                    gameSessions.remove(gameID);
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