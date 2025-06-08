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

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("WebSocket connected to server.");
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
            messageQueue.put(serverMessage);
        } catch (Exception e) {
            System.err.println("Error parsing WebSocket message: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
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