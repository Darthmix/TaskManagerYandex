package service;

import model.EpicTask;
import model.SingleTask;
import model.SubTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.io.Writer;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    private void createTestFile() {
        try {
            tempFile = File.createTempFile("TestTaskStorage", ".csv");
            tempFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FileBackedTaskManager createTestManager() {
        createTestFile();
        return FileBackedTaskManager.loadFromFile(tempFile.toPath());
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

        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1", LocalDateTime.now(), 15);
        taskManager.createTask(singleTask1);
        EpicTask epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
        taskManager.createTask(epicTask1);
        SubTask subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask1.getId());
        taskManager.createTask(subTask1);

        String[] linesOfFile = Files.readString(tempFile.toPath()).split("\n");

        Assertions.assertEquals("id,type,name,status,description,startTime,duration,epic", linesOfFile[0], "Ошибка записи заголовка");
        Assertions.assertEquals(taskManager.getTaskById(singleTask1.getId()).toString(), linesOfFile[1], "Ошибка записи обычной задачи");
        Assertions.assertEquals(taskManager.getTaskById(epicTask1.getId()).toString(), linesOfFile[2], "Ошибка записи эпика");
        Assertions.assertEquals(taskManager.getTaskById(subTask1.getId()).toString(), linesOfFile[3], "Ошибка записи подзадачи");
    }

    @Test
    void readTasksFromFile() {

        String[] content = new String[]{"id,type,name,status,description,startTime,duration,epic", "0,REG,CommonTask1,NEW,Common task 1,00:00 01.01.0001,0", "1,EPIC,EpicTask1,IN_PROGRESS,Epic task 1", "2,SUB,SubTask1,IN_PROGRESS,Subtask 1,00:00 01.01.0001,0,1"};

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
