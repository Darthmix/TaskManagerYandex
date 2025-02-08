package server;

import service.*;
import model.*;
import com.google.gson.Gson;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.List;

import com.google.gson.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryHandlerTest {

    private final TaskManager taskManager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(taskManager);
    private final Gson gson = new BaseHttpHandler().getGson();

    public HistoryHandlerTest() throws IOException {
    }

    @BeforeEach
    public void initialization() {
        taskManager.clearSingleTasks();
        taskManager.clearEpicTasks();
        taskManager.clearSubTasks();
        taskServer.start();
    }

    @AfterEach
    public void stopServer() {
        taskServer.stop();
    }

    @Test
    public void getHistoryTaskTest() throws IOException, InterruptedException {
        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1");
        taskManager.createTask(singleTask1);

        SingleTask singleTask2 = new SingleTask("CommonTask2", "Common task 2");
        taskManager.createTask(singleTask2);

        SingleTask singleTaskChanged = (SingleTask) taskManager.getTaskById(1);
        singleTaskChanged.setStatus(StatusTask.IN_PROGRESS);
        taskManager.updateTask(singleTaskChanged);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        List<SingleTask> serverTasks = gson.fromJson(response.body(), new TypeToken<List<SingleTask>>() {
        }.getType());

        assertEquals(1, serverTasks.size(), "Размер списка задач не совпадает");
        assertEquals(singleTask1, serverTasks.get(0), "Задачи не совпадают");
    }

    @Test
    public void notAllowedMethodForHistoryTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.NOT_ALLOWED.getCode(), response.statusCode());
    }
}
