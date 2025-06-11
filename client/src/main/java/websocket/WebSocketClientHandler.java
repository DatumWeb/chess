package websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;
import javax.websocket.*;

@ClientEndpoint
public class WebSocketClientHandler {
    private Session session;
    private final Gson gson = new Gson();
    private final LinkedBlockingQueue<ServerMessage> messageQueue = new LinkedBlockingQueue<>();

    public WebSocketClientHandler(String serverUri) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(serverUri));
        } catch (Exception e) {
            System.err.println("WebSocket connection error: " + e.getMessage());
        }
    }

    public void sendMessage(UserGameCommand command) {
        if (session != null && session.isOpen()) {
            String jsonMessage = gson.toJson(command);
            session.getAsyncRemote().sendText(jsonMessage);
        } else {
            System.err.println("WebSocket session is not open.");
        }
    }

    public ServerMessage getNextMessage() {
        return messageQueue.poll();
    }
}