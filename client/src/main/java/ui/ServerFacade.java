package ui;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthResult register(String username, String password, String email) throws Exception {
        if (username == null || username.isEmpty() || password == null || password.isEmpty() || email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Invalid registration data: Username, password, and email must be provided.");
        }
        var path = "/user";
        var body = Map.of("username", username, "password", password, "email", email);
        try {
            return this.makeRequest("POST", path, body, null, AuthResult.class);
        } catch (Exception e) {
            if (e.getMessage().contains("HTTP Error: 403")) {
                throw new Exception("User already exists");
            }
            throw e;
        }
    }

    public AuthResult login(String username, String password) throws Exception {
        var path = "/session";
        var body = Map.of("username", username, "password", password);
        try {
            return this.makeRequest("POST", path, body, null, AuthResult.class);
        } catch (Exception e) {
            if (e.getMessage().contains("HTTP Error: 401")) {
                throw new Exception("Invalid username or password");
            }
            throw e;
        }
    }

    public void logout(String authToken) throws Exception {
        var path = "/session";
        try {
            this.makeRequest("DELETE", path, null, authToken, null);
        } catch (Exception e) {
            if (e.getMessage().contains("HTTP Error: 401")) {
                throw new Exception("Invalid authentication token");
            }
            throw e;
        }
    }

    public GameResult createGame(String gameName, String authToken) throws Exception {
        if (gameName == null || gameName.isEmpty()) {
            throw new IllegalArgumentException("Game name cannot be null or empty");
        }
        var path = "/game";
        var body = Map.of("gameName", gameName);
        try {
            return this.makeRequest("POST", path, body, authToken, GameResult.class);
        } catch (Exception e) {
            if (e.getMessage().contains("HTTP Error: 401")) {
                throw new Exception("Invalid authentication token");
            }
            throw e;
        }
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        var path = "/game";
        try {
            return this.makeRequest("GET", path, null, authToken, ListGamesResult.class);
        } catch (Exception e) {
            if (e.getMessage().contains("HTTP Error: 401")) {
                throw new Exception("Invalid authentication token");
            }
            throw e;
        }
    }

    public void joinGame(int gameID, String playerColor, String authToken) throws Exception {
        var path = "/game";
        var body = Map.of("gameID", gameID, "playerColor", playerColor);
        try {
            this.makeRequest("PUT", path, body, authToken, null);
        } catch (Exception e) {
            if (e.getMessage().contains("HTTP Error: 401")) {
                throw new Exception("Invalid authentication token");
            } else if (e.getMessage().contains("HTTP Error: 403")) {
                throw new Exception("Color already taken or forbidden action");
            } else if (e.getMessage().contains("HTTP Error: 400") ||
                    e.getMessage().contains("HTTP Error: 404") ||
                    e.getMessage().contains("HTTP Error: 500")) {
                throw new Exception("Invalid game ID or player color");
            }
            throw e;
        }
    }

    public <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> responseClass) throws Exception {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeHeaders(http, authToken);
            writeBody(request, http);

            int statusCode = http.getResponseCode();
            if (statusCode >= 400) {
                throw new Exception("HTTP Error: " + statusCode + " - " + getErrorMessage(http));
            }

            return readBody(http, responseClass);
        } catch (Exception ex) {
            if (ex.getMessage().startsWith("HTTP Error:")) {
                throw ex;
            }
            throw new Exception("Request failed: " + ex.getMessage());
        }
    }

    private static void writeHeaders(HttpURLConnection http, String authToken) {
        http.addRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            http.addRequestProperty("Authorization", authToken);
        }
    }

    private void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            String reqData = gson.toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = gson.fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private String getErrorMessage(HttpURLConnection http) throws IOException {
        try (InputStream errorStream = http.getErrorStream()) {
            if (errorStream != null) {
                InputStreamReader reader = new InputStreamReader(errorStream);
                var errorResponse = gson.fromJson(reader, Map.class);
                return errorResponse.getOrDefault("message", "An unexpected error occurred").toString();
            }
        }
        return "An unexpected error occurred";
    }

    public static class AuthResult {
        public String username;
        public String authToken;
    }

    public static class GameResult {
        public int gameID;
    }

    public static class ListGamesResult {
        public GameInfo[] games;
    }

    public static class GameInfo {
        public int gameID;
        public String whiteUsername;
        public String blackUsername;
        public String gameName;
    }

    public void clear() throws Exception {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }
}