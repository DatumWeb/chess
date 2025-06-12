package websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;
import javax.websocket.*;

@ClientEndpoint
public class WebSocketClientHandler {
    private Session session;
    private final Gson gson = new Gson();
    private final LinkedBlockingQueue<ServerMessage> messageQueue = new LinkedBlockingQueue<>();

    public WebSocketClientHandler(String serverUrl) {
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("WebSocket connection established");
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ServerMessage serverMessage = parseServerMessage(message);
            if (serverMessage != null) {
                messageQueue.offer(serverMessage);
            }
        } catch (Exception e) {
            System.err.println("Error processing server message: " + e.getMessage());
        }
    }



    private ServerMessage parseServerMessage(String message) {
        try {
            com.google.gson.JsonObject jsonObject = gson.fromJson(message, com.google.gson.JsonObject.class);
            String messageType = jsonObject.get("serverMessageType").getAsString();

            switch (messageType) {
                case "LOAD_GAME":
                    return gson.fromJson(message, LoadGameMessage.class);
                case "ERROR":
                    return gson.fromJson(message, ErrorMessage.class);
                case "NOTIFICATION":
                    return gson.fromJson(message, NotificationMessage.class);
                default:
                    System.err.println("Unknown message type: " + messageType);
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Error parsing server message: " + e.getMessage());
            return null;
        }
    }

    public void sendMessage(UserGameCommand command) {
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = gson.toJson(command);
                session.getBasicRemote().sendText(jsonMessage);
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        } else {
            System.err.println("WebSocket session is not open.");
        }
    }

    public ServerMessage getNextMessage() {
        return messageQueue.poll();
    }

    public void connect(String serverUrl) {
        try {
            if (session != null && session.isOpen()) {
                return;
            }

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, new URI(serverUrl));

            Thread.sleep(100);
        } catch (Exception e) {
            System.err.println("WebSocket connection error: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

}