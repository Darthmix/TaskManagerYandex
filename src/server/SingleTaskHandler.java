package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import model.SingleTask;
import model.Task;
import service.*;

public class SingleTaskHandler extends BaseHttpHandler implements HttpHandler {

    protected final TaskManager taskManager;

    public SingleTaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            HttpRequestMethods requestMethod = HttpRequestMethods.valueOf(httpExchange.getRequestMethod());
            switch (requestMethod) {
                case GET:
                    getByRequest(httpExchange);
                    break;
                case POST:
                    postByRequest(httpExchange);
                    break;
                case DELETE:
                    deleteByRequest(httpExchange);
                    break;
            }
        } catch (TaskTimeOverlapException taskTimeOverlapException) {
            System.out.println(taskTimeOverlapException.getMessage());
            sendHasOverlap(httpExchange);
        } catch (NotFoundException notFoundException) {
            System.out.println(notFoundException.getMessage());
            sendNotFound(httpExchange, notFoundException.getMessage());
        } catch (Exception exception) {
            httpExchange.sendResponseHeaders(HttpCodeResponse.SERVER_ERROR.getCode(), 0);
        } finally {
            httpExchange.close();
        }
    }

    protected void deleteByRequest(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        if (Pattern.matches("^/singletasks$", path)) {
            taskManager.clearSingleTasks();
            httpExchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
            return;
        }
        if (Pattern.matches("^/singletasks/\\d+$", path)) {
            Integer id = parseTaskId(path.replaceFirst("/singletasks/", ""));
            if (id != -1) {
                taskManager.removeTask(id);
                httpExchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
            }
        } else {
            sendMethodNotAllowed(httpExchange);
        }
    }

    protected void getByRequest(HttpExchange httpExchange) throws IOException, NotFoundException {
        String path = httpExchange.getRequestURI().getPath();
        if (Pattern.matches("^/singletasks$", path)) {
            String response = getGson().toJson(taskManager.getSingleTasks());
            sendResponse(httpExchange, response);
            return;
        }
        if (Pattern.matches("^/singletasks/\\d+$", path)) {
            Integer id = parseTaskId(path.replaceFirst("/singletasks/", ""));
            if (id != -1) {
                Task singleTask = taskManager.getTaskById(id);
                sendResponse(httpExchange, getGson().toJson(singleTask));
            }
        } else {
            sendMethodNotAllowed(httpExchange);
        }
    }

    protected void postByRequest(HttpExchange httpExchange) throws IOException, TaskTimeOverlapException {
        String path = httpExchange.getRequestURI().getPath();
        String body = new String(httpExchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        Task singleTask = getGson().fromJson(body, SingleTask.class);
        if (Pattern.matches("^/singletasks$", path)) {
            if (singleTask.getId() == 0) {
                taskManager.createTask(singleTask);
            } else {
                taskManager.updateTask(singleTask);
            }
            httpExchange.sendResponseHeaders(HttpCodeResponse.MODIFIED.getCode(), 0);
        } else {
            sendMethodNotAllowed(httpExchange);
        }
    }

}
