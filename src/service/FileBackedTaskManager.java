package service;

import model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String TITLE = "id,type,name,status,description,epic";
    private final Path path;

    public FileBackedTaskManager(Path path) {
        super();
        this.path = path;
        if (Files.notExists(path)) {
            createTasksStorage(path);
        }
    }

    private void createTasksStorage(Path path) {
        try {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.createFile(path);
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка создания файла для сохранения: " + exception.getMessage());
        }
    }

    public void save() {
        try (Writer fileWriter = new FileWriter(path.toString(), StandardCharsets.UTF_8)) {
            fileWriter.write(TITLE + "\n");
            for (Task task : getSingleTasks()) {
                fileWriter.write(task.toString() + "\n");
            }
            for (Task task : getEpicTasks()) {
                fileWriter.write(task.toString() + "\n");
            }
            for (Task task : getSubTasks()) {
                fileWriter.write(task.toString() + "\n");
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка авто сохранения данных: " + exception.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(Path path) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(path);
        int countID = -1;
        try {
            // Считывание файла
            String content = Files.readString(path);

            // Проверка на наличие данных
            if (content.length() < TITLE.length()) return taskManager;

            // Получение строк с данными
            String[] lines = content.substring(TITLE.length() + 1).split("\n");
            if (!lines[0].isEmpty()) {
                // Обработка данных построчно
                for (String line : lines) {
                    Task task = fromString(line);
                    if (countID < task.getId()) {
                        countID = task.getId();
                    }
                    taskManager.updateTask(task);
                }
            }
            taskManager.setNextFreeId(countID);
            return taskManager;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static Task fromString(String value) {
        String[] fieldsOfTask = value.split(",");
        int id = Integer.parseInt(fieldsOfTask[0]);
        TypeTask typeTask = TypeTask.valueOf(fieldsOfTask[1]);
        String name = fieldsOfTask[2];
        StatusTask statusTask = StatusTask.valueOf(fieldsOfTask[3]);
        String description = fieldsOfTask[4];

        switch (typeTask) {
            case REG:
                SingleTask singleTask = new SingleTask(name, description, statusTask);
                singleTask.setId(id);
                return singleTask;
            case EPIC:
                EpicTask epicTask = new EpicTask(name, description);
                epicTask.setStatus(statusTask);
                epicTask.setId(id);
                return epicTask;
            default:
                Integer epicId = Integer.parseInt(fieldsOfTask[5]);
                SubTask subTask = new SubTask(name, description, epicId, statusTask);
                subTask.setId(id);
                return subTask;
        }
    }

    @Override
    public void removeTask(Integer id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void clearSingleTasks() {
        super.clearSingleTasks();
        save();
    }

    @Override
    public void clearEpicTasks() {
        super.clearEpicTasks();
        save();
    }

    @Override
    public void clearSubTasks() {
        super.clearSubTasks();
        save();
    }


}
