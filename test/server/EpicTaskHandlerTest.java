package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.time.temporal.ChronoUnit;

public class EpicTaskHandlerTest {
    private final TaskManager taskManager;
    private final HttpTaskServer httpTaskServer;
    private final Gson gson;
    private final LocalDateTime startTime;
    private Task epicTask;

    HttpClient client;

    public EpicTaskHandlerTest() throws IOException {
        taskManager = Managers.getDefaultTaskManager();
        httpTaskServer = new HttpTaskServer(taskManager);
        gson = new BaseHttpHandler().getGson();
        startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        //startTime = LocalDateTime.now();
    }

    @BeforeEach
    public void initialization() {
        taskManager.clearEpicTasks();
        epicTask = new EpicTask("EpicTask1", "Epic task 1");
        httpTaskServer.start();
    }

    @AfterEach
    public void stopServer() {
        httpTaskServer.stop();
    }

    @Test
    public void getEpicTasksTest() throws IOException, InterruptedException {
        taskManager.createTask(epicTask);

        Task epicTask1 = new EpicTask("CommonTask2", "Common task 2");
        taskManager.createTask(epicTask1);

        SubTask subTask1 = new SubTask("SubTask1", "Subtask 1", startTime, 60, epicTask1.getId());
        taskManager.createTask(subTask1);
        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(2), 60, epicTask1.getId());
        taskManager.createTask(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        List<Task> epicTasksResponse = gson.fromJson(response.body(), new TypeToken<List<EpicTask>>() {
        }.getType());

        assertEquals(epicTask, epicTasksResponse.get(0), "Задачи не совпадают");
        assertEquals(epicTask1, epicTasksResponse.get(1), "Задачи не совпадают");
    }

    @Test
    public void getEpicTasksByIdTest() throws IOException, InterruptedException {
        taskManager.createTask(epicTask);

        Task epicTask1 = new EpicTask("CommonTask2", "Common task 2");
        taskManager.createTask(epicTask1);

        SubTask subTask1 = new SubTask("SubTask1", "Subtask 1", startTime, 60, epicTask1.getId());
        taskManager.createTask(subTask1);
        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(2), 60, epicTask1.getId());
        taskManager.createTask(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicTask1.getId());

        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        Task epicTasksResponse = gson.fromJson(response.body(), EpicTask.class);

        assertEquals(epicTask1, epicTasksResponse, "Задачи не совпадают");

    }

    @Test
    public void getEpicTasksByWrongIdTest() throws IOException, InterruptedException {
        taskManager.createTask(epicTask);

        Task epicTask1 = new EpicTask("CommonTask2", "Common task 2");
        taskManager.createTask(epicTask1);

        SubTask subTask1 = new SubTask("SubTask1", "Subtask 1", startTime, 60, epicTask1.getId());
        taskManager.createTask(subTask1);
        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(2), 60, epicTask1.getId());
        taskManager.createTask(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/9999");

        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.NOT_FOUND.getCode(), response.statusCode());
    }

    @Test
    public void getSubtasksEpicTest() throws IOException, InterruptedException {
        taskManager.createTask(epicTask);

        Task epicTask1 = new EpicTask("CommonTask2", "Common task 2");
        taskManager.createTask(epicTask1);

        SubTask subTask1 = new SubTask("SubTask1", "Subtask 1", startTime, 60, epicTask1.getId());
        taskManager.createTask(subTask1);
        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(2), 60, epicTask1.getId());
        taskManager.createTask(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicTask1.getId() + "/subtasks");

        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<SubTask> subTasksTasks = gson.fromJson(response.body(), new TypeToken<List<SubTask>>() {
        }.getType());

        assertEquals(subTask1, subTasksTasks.get(0), "Задачи не совпадают");
        assertEquals(subTask2, subTasksTasks.get(1), "Задачи не совпадают");
    }

    @Test
    public void createEpicTask() throws IOException, InterruptedException {

        String epicTaskJson = gson.toJson(epicTask);

        client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(epicTaskJson))
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode(), "Ошибка сервера");

        List<Task> tasksFromManager = taskManager.getEpicTasks();
        assertNotNull(tasksFromManager, "Задача не создаётся");
    }

    @Test
    public void updateEpicTask() throws IOException, InterruptedException {
        taskManager.createTask(epicTask);

        Task epicTask1 = new EpicTask("CommonTask2", "Common task 2");
        epicTask1.setId(1);

        String singleTaskJson = gson.toJson(epicTask1);

        client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(singleTaskJson))
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode(), "Ошибка сервера");
        List<Task> tasksFromManager = taskManager.getEpicTasks();
        assertEquals(tasksFromManager.size(), 1, "Задача создалась, а не обновилась");
        assertEquals("CommonTask2", tasksFromManager.get(0).getName(), "Ошибка обновления задачи");
    }

    @Test
    public void removeTaskById() throws IOException, InterruptedException {
        taskManager.createTask(epicTask);

        Task epicTask1 = new EpicTask("CommonTask2", "Common task 2");
        taskManager.createTask(epicTask1);

        SubTask subTask1 = new SubTask("SubTask1", "Subtask 1", startTime, 60, epicTask1.getId());
        taskManager.createTask(subTask1);
        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(2), 60, epicTask1.getId());
        taskManager.createTask(subTask2);

        client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/epics/" + epicTask1.getId());

        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertEquals(1, taskManager.getEpicTasks().size(), "Задача не удаляется");

    }

    @Test
    public void removeAllEpicTasks() throws IOException, InterruptedException {
        taskManager.createTask(epicTask);

        Task epicTask1 = new EpicTask("CommonTask2", "Common task 2");
        taskManager.createTask(epicTask1);

        SubTask subTask1 = new SubTask("SubTask1", "Subtask 1", startTime, 60, epicTask1.getId());
        taskManager.createTask(subTask1);
        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(2), 60, epicTask1.getId());
        taskManager.createTask(subTask2);

        client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/epics");

        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertEquals(0, taskManager.getEpicTasks().size(), "Задачи не удаляется");
    }


}
