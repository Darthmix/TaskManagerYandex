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
import java.time.LocalDateTime;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PrioritizedHandlerTest {
    private final TaskManager taskManager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(taskManager);
    private final Gson gson = new BaseHttpHandler().getGson();
    private final LocalDateTime startTime = LocalDateTime.now();

    public PrioritizedHandlerTest() throws IOException {
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
    public void getPrioritizedTasks() throws IOException, InterruptedException {
        SingleTask singleTaskNoTime = new SingleTask("CommonTask1", "Common task 1");
        taskManager.createTask(singleTaskNoTime);
        SingleTask singleTaskWithTime1 = new SingleTask("CommonTask2", "Common task 2", startTime, 30);
        taskManager.createTask(singleTaskWithTime1);
        SingleTask singleTaskWithTime2 = new SingleTask("CommonTask2", "Common task 2", startTime.plusHours(1), 30);
        taskManager.createTask(singleTaskWithTime2);
        SingleTask singleTaskWithTime3 = new SingleTask("CommonTask2", "Common task 2", startTime.minusHours(1), 30);
        taskManager.createTask(singleTaskWithTime3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.OK.getCode(), response.statusCode());
        List<SingleTask> serverTasks = gson.fromJson(response.body(), new TypeToken<List<SingleTask>>() {
        }.getType());

        assertFalse(serverTasks.contains(singleTaskNoTime),
                    "Задача без времени содержится в приоритезированных задачах");
        assertEquals(singleTaskWithTime3, serverTasks.get(0), "Не правильно определена первая задача");
        assertEquals(singleTaskWithTime2, serverTasks.get(2), "Не правильно определена последняя задача");

    }

    @Test
    public void notAllowedMethodForHistoryTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpCodeResponse.NOT_ALLOWED.getCode(), response.statusCode());
    }
}
