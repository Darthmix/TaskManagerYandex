package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubTaskHandlerTest {

    private final TaskManager taskManager;
    private final HttpTaskServer httpTaskServer;
    private final Gson gson;
    private final LocalDateTime startTime;
    private Task epicTask;
    private Task subTask;

    HttpClient client;

    public SubTaskHandlerTest() throws IOException {
        taskManager = Managers.getDefaultTaskManager();
        httpTaskServer = new HttpTaskServer(taskManager);
        gson = new BaseHttpHandler().getGson();
        startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        //startTime = LocalDateTime.now();
    }

    @BeforeEach
    public void initialization() {
        taskManager.clearEpicTasks();

        epicTask = new EpicTask("CommonTask1", "Common task 1");
        taskManager.createTask(epicTask);

        subTask = new SubTask("SubTask1", "Subtask 1", startTime, 60, epicTask.getId());
        httpTaskServer.start();

    }

    @AfterEach
    public void stopServer() {
        httpTaskServer.stop();
    }

    @Test
    public void getSubTasksTest() throws IOException, InterruptedException {
        taskManager.createTask(subTask);

        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(2), 60, epicTask.getId());
        taskManager.createTask(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        List<Task> subTasksResponse = gson.fromJson(response.body(), new TypeToken<List<SubTask>>() {
        }.getType());

        assertEquals(subTask, subTasksResponse.get(0), "Задачи не совпадают");
        assertEquals(subTask2, subTasksResponse.get(1), "Задачи не совпадают");
    }

    @Test
    public void getSubTaskByIdTest() throws IOException, InterruptedException {
        taskManager.createTask(subTask);
        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(2), 60, epicTask.getId());
        taskManager.createTask(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subTask2.getId());

        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        Task subTaskResponse = gson.fromJson(response.body(), SubTask.class);

        assertEquals(subTask2, subTaskResponse, "Задачи не совпадают");
    }

    @Test
    public void getSubTaskByWrongIdTest() throws IOException, InterruptedException {
        taskManager.createTask(subTask);
        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(2), 60, epicTask.getId());
        taskManager.createTask(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/9999");

        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.NOT_FOUND.getCode(), response.statusCode());
    }

    @Test
    public void createSubTask() throws IOException, InterruptedException {

        String subTaskJson = gson.toJson(subTask);

        client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode(), "Ошибка сервера");

        List<Task> tasksFromManager = taskManager.getSubTasks();
        assertNotNull(tasksFromManager, "Задача не создаётся");
    }

    @Test
    public void updateSingleTask() throws IOException, InterruptedException {
        taskManager.createTask(subTask);

        SubTask subTaskChanged = new SubTask("SubTask1", "Subtask 1", startTime, 60, epicTask.getId());
        subTaskChanged.setId(2);
        subTaskChanged.setStatus(StatusTask.DONE);

        String singleTaskJson = gson.toJson(subTaskChanged);

        client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(singleTaskJson))
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode(), "Ошибка сервера");
        List<Task> tasksFromManager = taskManager.getSubTasks();
        assertEquals(tasksFromManager.size(), 1, "Задача создалась, а не обновилась");
        assertEquals(StatusTask.DONE, tasksFromManager.get(0).getStatusTask(), "Ошибка обновления задачи");
    }

    @Test
    public void createOverlapTask() throws IOException, InterruptedException {
        taskManager.createTask(subTask);

        SubTask subTaskOverlap = new SubTask("CommonTaskOverlap",
                                             "Common task Overlap",
                                             startTime.plusMinutes(30),
                                             60,
                                             epicTask.getId());
        String singleTaskJson = gson.toJson(subTaskOverlap);

        client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(singleTaskJson))
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpCodeResponse.OVERLAP.getCode(), response.statusCode(), "Ошибка сервера");
        assertEquals(1, taskManager.getSubTasks().size(), "Некорректное количество задач");
    }

    @Test
    public void removeTaskById() throws IOException, InterruptedException {
        taskManager.createTask(subTask);

        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(2), 60, epicTask.getId());
        taskManager.createTask(subTask2);

        client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/subtasks/" + subTask2.getId());

        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertEquals(1, taskManager.getSubTasks().size(), "Задача не удаляется");
    }

    @Test
    public void removeAllSingleTasks() throws IOException, InterruptedException {
        taskManager.createTask(subTask);

        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(2), 60, epicTask.getId());
        taskManager.createTask(subTask2);

        client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/subtasks");

        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertEquals(0, taskManager.getSubTasks().size(), "Задачи не удаляется");
    }


}
