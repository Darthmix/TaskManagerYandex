package service;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> taskById;  // Основной хеш список всех тасок
    private Integer taskIdGenerator; // Объект генерации новых ID для тасок
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        taskById = new HashMap<>();
        taskIdGenerator = 0;
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = taskById.get(id);
        historyManager.add(task);
        return task;
    }

    private List<Task> getTasksByType(TypeTask typeTask) {
        List<Task> tasks = new ArrayList<>();
        for (Task task : taskById.values()) {
            if (task.getTypeTask().equals(typeTask)) {
                historyManager.add(task);
                tasks.add(task);
            }
        }
        return tasks;
    }

    @Override
    public List<Task> getSingleTasks() {
        return getTasksByType(TypeTask.REG);
    }

    @Override
    public List<Task> getSubTasks() {
        return getTasksByType(TypeTask.SUB);
    }

    @Override
    public List<Task> getEpicTasks() {
        return getTasksByType(TypeTask.EPIC);
    }

    @Override
    public void removeTask(Integer id) {
        Task task = getTaskById(id);
        switch (task.getTypeTask()) {
            case REG:
                taskById.remove(task.getId());
                break;
            case SUB:
                SubTask subTask = (SubTask) task;
                subTask.removeFromEpic((EpicTask) taskById.get(subTask.getEpicId()));
                taskById.remove(task.getId());
                break;
            case EPIC:
                EpicTask epicTask = (EpicTask) task;
                List<SubTask> subTasks = epicTask.getSubTasks();
                for (SubTask subtask : subTasks) {
                    taskById.remove(subtask.getId());
                }
                taskById.remove(task.getId());
                break;
        }
    }

    private void clearByType(TypeTask typeTask) {
        List<Task> tasks = getTasksByType(typeTask);
        for (Task task : tasks) {
            removeTask(task.getId());
        }
    }

    @Override
    public void clearSingleTasks() {
        clearByType(TypeTask.REG);
    }

    @Override
    public void clearEpicTasks() {
        clearByType(TypeTask.EPIC);
        clearByType(TypeTask.SUB);
    }

    @Override
    public void clearSubTasks() {
        List<Task> subTasks = getSubTasks();

        for (Task task : subTasks) {
            SubTask subTask = (SubTask) task;
            subTask.removeFromEpic((EpicTask) getTaskById(subTask.getEpicId()));
        }
        clearByType(TypeTask.SUB);
    }

    @Override
    public void createTask(Task task) {
        task.setId(getNextFreeId());
        taskById.put(task.getId(), task);
        if (task.getTypeTask().equals(TypeTask.SUB)) {
            SubTask subTask = (SubTask) task;
            EpicTask epicTask = (EpicTask) taskById.get(subTask.getEpicId());
            epicTask.modifySubTask(subTask);
        }
    }

    @Override
    public void updateTask(Task task) {
        taskById.put(task.getId(), task);
        if (task.getTypeTask().equals(TypeTask.SUB)) {
            SubTask subTask = (SubTask) task;
            EpicTask epicTask = (EpicTask) taskById.get(subTask.getEpicId());
            epicTask.modifySubTask(subTask);
        }
    }

    private Integer getNextFreeId() {
        return taskIdGenerator++;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}