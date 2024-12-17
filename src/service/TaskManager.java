package service;

import model.*;

import java.util.List;

public interface TaskManager {

    void clearSingleTasks();

    void clearEpicTasks();

    void clearSubTasks();

    Task getTaskById(int id);

    List<Task> getSingleTasks();

    List<Task> getSubTasks();

    List<Task> getEpicTasks();

    void removeTask(Integer id);

    void createTask(Task task);

    void updateTask(Task task);

    List<Task> getHistory();

}
