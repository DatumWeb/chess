import chess.*;

import ui.ChessClient;

public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        if (args.length > 0) {
            serverUrl = args[0];
        }

        ChessClient client = new ChessClient(serverUrl);
        client.run();
    }
}