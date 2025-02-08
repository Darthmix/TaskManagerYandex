package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.SubTask;
import model.Task;
import service.NotFoundException;
import service.TaskManager;
import service.TaskTimeOverlapException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class SubTaskHandler extends BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;

    public SubTaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            String requestMethod = httpExchange.getRequestMethod();
            switch (requestMethod) {
                case "GET":
                    getByRequest(httpExchange);
                    break;
                case "POST":
                    postByRequest(httpExchange);
                    break;
                case "DELETE":
                    deleteByRequest(httpExchange);
                    break;
                default:
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

    protected void getByRequest(HttpExchange httpExchange) throws IOException, NotFoundException {
        String path = httpExchange.getRequestURI().getPath();
        List<Task> subTasks = taskManager.getSubTasks();
        if (Pattern.matches("^/subtasks$", path)) {
            String response = getGson().toJson(subTasks);
            sendResponse(httpExchange, response);
            return;
        }
        if (Pattern.matches("^/subtasks/\\d+$", path)) {
            Integer id = parseTaskId(path.replaceFirst("/subtasks/", ""));
            if (id != -1) {
                Optional<Task> subTask = subTasks.stream().filter(taskTmp -> taskTmp.getId() == id).findFirst();
                if (subTask.isPresent()) {
                    sendResponse(httpExchange, getGson().toJson(subTask.get()));
                } else {
                    throw new NotFoundException("Задача не найдена в списке. id: " + id);
                }
            }
        } else {
            sendMethodNotAllowed(httpExchange);
        }
    }

    protected void postByRequest(HttpExchange httpExchange) throws IOException, TaskTimeOverlapException {
        String path = httpExchange.getRequestURI().getPath();
        String body = new String(httpExchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        Task subTask = getGson().fromJson(body, SubTask.class);
        if (Pattern.matches("^/subtasks$", path)) {
            if (subTask.getId() == 0) {
                taskManager.createTask(subTask);
            } else {
                taskManager.updateTask(subTask);
            }
            httpExchange.sendResponseHeaders(HttpCodeResponse.MODIFIED.getCode(), 0);
        } else {
            sendMethodNotAllowed(httpExchange);
        }
    }

    protected void deleteByRequest(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        if (Pattern.matches("^/subtasks$", path)) {
            taskManager.clearSubTasks();
            httpExchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
            return;
        }
        if (Pattern.matches("^/subtasks/\\d+$", path)) {
            Integer id = parseTaskId(path.replaceFirst("/subtasks/", ""));
            if (id != -1) {
                taskManager.removeTask(id);
                httpExchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
            }
        } else {
            sendMethodNotAllowed(httpExchange);
        }
    }

}
