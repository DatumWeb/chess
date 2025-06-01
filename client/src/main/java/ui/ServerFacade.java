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
        var path = "/user";
        var body = Map.of("username", username, "password", password, "email", email);
        return this.makeRequest("POST", path, body, null, AuthResult.class);
    }

    public AuthResult login(String username, String password) throws Exception {
        var path = "/session";
        var body = Map.of("username", username, "password", password);
        return this.makeRequest("POST", path, body, null, AuthResult.class);
    }

    public void logout(String authToken) throws Exception {
        var path = "/session";
        this.makeRequest("DELETE", path, null, authToken, null);
    }

    private <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> responseClass) throws Exception {
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
                return (String) errorResponse.get("message");
            }
        }
        return http.getResponseMessage();
    }

    public static class AuthResult {
        public String username;
        public String authToken;
    }

}
