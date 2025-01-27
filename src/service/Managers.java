package service;

import java.nio.file.Path;

public abstract class Managers {

    private static final String PATH_TO_DATA = "src/data/TasksStorage.csv";

    private Managers() {
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefaultFileBackedTaskManager() {
        return new FileBackedTaskManager(Path.of(PATH_TO_DATA));
    }

    public static TaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager();
    }
}