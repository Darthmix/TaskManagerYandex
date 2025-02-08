package server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import model.EpicTask;
import model.Task;
import service.NotFoundException;
import service.TaskManager;
import service.TaskTimeOverlapException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


public class EpicTaskHandler extends BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;

    public EpicTaskHandler(TaskManager taskManager) {
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
        List<Task> epicTasks = taskManager.getEpicTasks();
        if (Pattern.matches("^/epics$", path)) {
            String response = getGson().toJson(epicTasks);
            sendResponse(httpExchange, response);
            return;
        }
        if (Pattern.matches("^/epics/\\d+$", path)) {
            Integer id = parseTaskId(path.replaceFirst("/epics/", ""));
            if (id != -1) {
                Optional<Task> epicTask = epicTasks.stream().filter(taskTmp -> taskTmp.getId() == id).findFirst();
                if (epicTask.isPresent()) {
                    sendResponse(httpExchange, getGson().toJson(epicTask.get()));
                } else {
                    throw new NotFoundException("Задача не найдена в списке. id: " + id);
                }
            }
        }

        if (Pattern.matches("^/epics/\\d+/subtasks$", path)) {
            String pathId = path.replaceFirst("/epics/", "").replaceFirst("/subtasks", "");
            Integer id = parseTaskId(pathId);
            if (id != -1) {
                Optional<Task> epicTask = epicTasks.stream().filter(taskTmp -> taskTmp.getId() == id).findFirst();
                if (epicTask.isPresent()) {
                    EpicTask epicTaskTmp = (EpicTask) epicTask.get();
                    sendResponse(httpExchange, getGson().toJson(epicTaskTmp.getSubTasks()));
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
        Task epicTask = getGson().fromJson(body, EpicTask.class);
        if (Pattern.matches("^/epics$", path)) {
            if (epicTask.getId() == 0) {
                taskManager.createTask(epicTask);
            } else {
                taskManager.updateTask(epicTask);
            }
            httpExchange.sendResponseHeaders(HttpCodeResponse.MODIFIED.getCode(), 0);
        } else {
            sendMethodNotAllowed(httpExchange);
        }
    }

    protected void deleteByRequest(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        if (Pattern.matches("^/epics$", path)) {
            taskManager.clearEpicTasks();
            httpExchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
            return;
        }
        if (Pattern.matches("^/epics/\\d+$", path)) {
            Integer id = parseTaskId(path.replaceFirst("/epics/", ""));
            if (id != -1) {
                taskManager.removeTask(id);
                httpExchange.sendResponseHeaders(HttpCodeResponse.OK.getCode(), 0);
            }
        } else {
            sendMethodNotAllowed(httpExchange);
        }
    }

}
