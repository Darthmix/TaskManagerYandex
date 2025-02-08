package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

import java.nio.charset.StandardCharsets;

import java.time.Duration;
import java.time.LocalDateTime;
import java.io.IOException;

public class BaseHttpHandler {

    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        return gsonBuilder.create();
    }

    protected void sendResponse(HttpExchange httpExchange, String text) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), response.length);
        httpExchange.getResponseBody().write(response);
    }

    protected void sendNotFound(HttpExchange httpExchange, String message) throws IOException {
        httpExchange.sendResponseHeaders(HttpCodeResponse.NOT_FOUND.getCode(), 0);
        httpExchange.getResponseBody().write(message.getBytes(StandardCharsets.UTF_8));
    }

    protected void sendHasOverlap(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(HttpCodeResponse.OVERLAP.getCode(), 0);
    }

    protected void sendMethodNotAllowed(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(HttpCodeResponse.NOT_ALLOWED.getCode(), 0);
    }

    protected Integer parseTaskId(String path) {
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException numberFormatException) {
            return -1;
        }
    }

}
