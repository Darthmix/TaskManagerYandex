package server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.NotFoundException;
import service.TaskManager;

import java.io.IOException;
import java.util.regex.Pattern;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            HttpRequestMethods requestMethod = HttpRequestMethods.valueOf(httpExchange.getRequestMethod());
            if (requestMethod.equals(HttpRequestMethods.GET)) {
                getByRequest(httpExchange);
            } else {
                System.out.println("Такой метод запроса не возможен");
                sendMethodNotAllowed(httpExchange);
            }
        } catch (Exception exception) {
            httpExchange.sendResponseHeaders(HttpCodeResponse.SERVER_ERROR.getCode(), 0);
        } finally {
            httpExchange.close();
        }
    }

    protected void getByRequest(HttpExchange httpExchange) throws IOException, NotFoundException {
        String path = httpExchange.getRequestURI().getPath();
        if (Pattern.matches("^/prioritized$", path)) {
            String response = getGson().toJson(taskManager.getPrioritizedTasks());
            sendResponse(httpExchange, response);
        } else {
            sendMethodNotAllowed(httpExchange);
        }
    }


}
