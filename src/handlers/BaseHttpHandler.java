package handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class BaseHttpHandler {
    protected final Gson gson;

    public BaseHttpHandler(Gson gson) {
        this.gson = gson;
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendSuccess(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 200);
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 201);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\": \"Not found\"}", 404);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\": \"Task time overlaps with existing tasks\"}", 406);
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\": \"Internal server error\"}", 500);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"error\": \"" + message + "\"}", 400);
    }

    protected <T> Optional<T> parseJson(HttpExchange exchange, Class<T> clazz) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return Optional.ofNullable(gson.fromJson(body, clazz));
        } catch (JsonSyntaxException e) {
            return Optional.empty();
        }
    }

    protected String getPathParameter(HttpExchange exchange, int index) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length > index) {
            return pathParts[index];
        }
        return null;
    }

    protected int getIntPathParameter(HttpExchange exchange, int index) {
        try {
            String param = getPathParameter(exchange, index);
            return param != null ? Integer.parseInt(param) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
