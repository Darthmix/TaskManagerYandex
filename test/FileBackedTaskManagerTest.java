import model.*;
import service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.io.Writer;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

public class FileBackedTaskManagerTest {
    private File tempFile;
    private TaskManager taskManager;

    @BeforeEach
    public void initializeManager() throws IOException {
        tempFile = File.createTempFile("TestTaskStorage", ".csv");
        tempFile.deleteOnExit();
        taskManager = new FileBackedTaskManager(tempFile.toPath());
    }

    @Test
    void saveAndLoadEmptyFile() {
        Assertions.assertTrue(taskManager.getSingleTasks().isEmpty(), "Список обычных задач не пуст");
        Assertions.assertTrue(taskManager.getEpicTasks().isEmpty(), "Список эпиков задач не пуст");
        Assertions.assertTrue(taskManager.getSubTasks().isEmpty(), "Список подзадач задач не пуст");
        taskManager = FileBackedTaskManager.loadFromFile(tempFile.toPath());
        Assertions.assertTrue(taskManager.getSingleTasks().isEmpty(), "Список обычных задач не пуст после загрузки");
        Assertions.assertTrue(taskManager.getEpicTasks().isEmpty(), "Список эпиков задач не пуст после загрузки");
        Assertions.assertTrue(taskManager.getSubTasks().isEmpty(), "Список подзадач задач не пуст после загрузки");
    }

    @Test
    void saveTasksToFile() throws IOException {

        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1");
        taskManager.createTask(singleTask1);
        EpicTask epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
        taskManager.createTask(epicTask1);
        SubTask subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask1.getId());
        taskManager.createTask(subTask1);

        String[] linesOfFile = Files.readString(tempFile.toPath()).split("\n");

        Assertions.assertEquals("id,type,name,status,description,epic", linesOfFile[0], "Ошибка записи заголовка");
        Assertions.assertEquals(taskManager.getTaskById(singleTask1.getId()).toString(), linesOfFile[1], "Ошибка записи обычной задачи");
        Assertions.assertEquals(taskManager.getTaskById(epicTask1.getId()).toString(), linesOfFile[2], "Ошибка записи эпика");
        Assertions.assertEquals(taskManager.getTaskById(subTask1.getId()).toString(), linesOfFile[3], "Ошибка записи подзадачи");
    }

    @Test
    void readTasksFromFile() {

        String[] content = new String[]{"id,type,name,status,description,epic", "0,REG,CommonTask1,NEW,Common task 1", "1,EPIC,EpicTask1,IN_PROGRESS,Epic task 1", "2,SUB,SubTask1,IN_PROGRESS,Subtask 1,1",};

        try (Writer fileWriter = new FileWriter(tempFile, StandardCharsets.UTF_8)) {
            fileWriter.write(String.join("\n", content));
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка записи в тестовый файл");
        }

        taskManager = FileBackedTaskManager.loadFromFile(tempFile.toPath());

        Assertions.assertEquals(content[1], taskManager.getTaskById(0).toString(), "Ошибка чтения обычной задачи");
        Assertions.assertEquals(content[2], taskManager.getTaskById(1).toString(), "Ошибка чтения эпика");
        Assertions.assertEquals(content[3], taskManager.getTaskById(2).toString(), "Ошибка чтения подзадачи");

    }


}
