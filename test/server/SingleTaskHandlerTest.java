package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.SingleTask;
import model.StatusTask;
import model.Task;
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

class SingleTaskHandlerTest {

    private final TaskManager taskManager;
    private final HttpTaskServer httpTaskServer;
    private final Gson gson;
    private final LocalDateTime startTime;
    private Task singleTask;

    HttpClient client;

    public SingleTaskHandlerTest() throws IOException {
        taskManager = Managers.getDefaultTaskManager();
        httpTaskServer = new HttpTaskServer(taskManager);
        gson = new BaseHttpHandler().getGson();
        startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        //startTime = LocalDateTime.now();
    }

    @BeforeEach
    public void initialization() {
        taskManager.clearSingleTasks();
        singleTask = new SingleTask("CommonTask1", "Common task 1", startTime, 60);
        httpTaskServer.start();

    }

    @AfterEach
    public void stopServer() {
        httpTaskServer.stop();
    }

    @Test
    public void getSingleTasksTest() throws IOException, InterruptedException {
        taskManager.createTask(singleTask);
        Task singleTask1 = new SingleTask("CommonTask2", "Common task 2", startTime.plusHours(2), 60);
        taskManager.createTask(singleTask1);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/singletasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        List<Task> singleTasksResponse = gson.fromJson(response.body(), new TypeToken<List<SingleTask>>() {
        }.getType());

        assertEquals(singleTask, singleTasksResponse.get(0), "Задачи не совпадают");
        assertEquals(singleTask1, singleTasksResponse.get(1), "Задачи не совпадают");
    }

    @Test
    public void getSingleTasksByIdTest() throws IOException, InterruptedException {
        taskManager.createTask(singleTask);
        SingleTask singleTask1 = new SingleTask("CommonTask2", "Common task 2", startTime.plusHours(2), 60);
        singleTask1.setStatus(StatusTask.DONE);
        taskManager.createTask(singleTask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/singletasks/" + singleTask1.getId());

        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());

        Task singleTaskResponse = gson.fromJson(response.body(), SingleTask.class);

        assertEquals(singleTask1, singleTaskResponse, "Задачи не совпадают");

    }

    @Test
    public void getSingleTasksByWrongIdTest() throws IOException, InterruptedException {
        taskManager.createTask(singleTask);
        SingleTask singleTask1 = new SingleTask("CommonTask2", "Common task 2", startTime.plusHours(2), 60);
        singleTask1.setStatus(StatusTask.DONE);
        taskManager.createTask(singleTask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/singletasks/9999");

        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.NOT_FOUND.getCode(), response.statusCode());
    }

    @Test
    public void createSingleTask() throws IOException, InterruptedException {

        String singleTaskJson = gson.toJson(singleTask);

        client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/singletasks");

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(singleTaskJson))
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode(), "Ошибка сервера");

        List<Task> tasksFromManager = taskManager.getSingleTasks();
        assertNotNull(tasksFromManager, "Задача не создаётся");
    }

    @Test
    public void updateSingleTask() throws IOException, InterruptedException {
        taskManager.createTask(singleTask);

        SingleTask singleTaskChanged = new SingleTask("CommonTask1", "Common task 1", startTime, 60);
        singleTaskChanged.setId(1);
        singleTaskChanged.setStatus(StatusTask.DONE);

        String singleTaskJson = gson.toJson(singleTaskChanged);

        client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/singletasks");

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(singleTaskJson))
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.MODIFIED.getCode(), response.statusCode(), "Ошибка сервера");
        List<Task> tasksFromManager = taskManager.getSingleTasks();
        assertEquals(tasksFromManager.size(), 1, "Задача создалась, а не обновилась");
        assertEquals(StatusTask.DONE, tasksFromManager.get(0).getStatusTask(), "Ошибка обновления задачи");
    }

    @Test
    public void createOverlapTask() throws IOException, InterruptedException {
        taskManager.createTask(singleTask);

        SingleTask singleTaskOverlap = new SingleTask("CommonTaskOverlap",
                                                      "Common task Overlap",
                                                      startTime.plusMinutes(30),
                                                      60);
        String singleTaskJson = gson.toJson(singleTaskOverlap);

        client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/singletasks");

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(singleTaskJson))
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpCodeResponse.OVERLAP.getCode(), response.statusCode(), "Ошибка сервера");
        assertEquals(1, taskManager.getSingleTasks().size(), "Некорректное количество задач");
    }

    @Test
    public void removeTaskById() throws IOException, InterruptedException {
        taskManager.createTask(singleTask);

        SingleTask singleTaskRemove = new SingleTask("CommonTask1", "Common task 1", startTime, 60);
        singleTaskRemove.setStatus(StatusTask.DONE);
        taskManager.createTask(singleTaskRemove);

        client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/singletasks/" + singleTaskRemove.getId());

        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertEquals(1, taskManager.getSingleTasks().size(), "Задача не удаляется");

    }

    @Test
    public void removeAllSingleTasks() throws IOException, InterruptedException {
        taskManager.createTask(singleTask);

        SingleTask singleTaskRemove = new SingleTask("CommonTask1", "Common task 1", startTime, 60);
        singleTaskRemove.setStatus(StatusTask.DONE);
        taskManager.createTask(singleTaskRemove);

        client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/singletasks");

        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        assertEquals(0, taskManager.getSingleTasks().size(), "Задачи не удаляется");
    }
}