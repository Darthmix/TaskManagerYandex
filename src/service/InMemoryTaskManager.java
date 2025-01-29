package service;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> taskById;  // Основной хеш список всех тасок
    private Integer taskIdGenerator; // Объект генерации новых ID для тасок
    private final HistoryManager historyManager;

    private final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    private static final LocalDateTime NO_TIME = LocalDateTime.of(1, 1, 1, 0, 0);
    private static final String overlapError = "Новая задача пересекается по времени с уже существующими задачами";

    public InMemoryTaskManager() {
        taskById = new HashMap<>();
        taskIdGenerator = 0;
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public Task getTaskById(int id) throws NotFoundException {
        if (!taskById.containsKey(id)) throw new NotFoundException("Задача не найдена в списке. id: " + id);
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
                historyManager.remove(task.getId());
                break;
            case SUB:
                SubTask subTask = (SubTask) task;
                subTask.removeFromEpic((EpicTask) taskById.get(subTask.getEpicId()));
                taskById.remove(task.getId());
                historyManager.remove(task.getId());
                break;
            case EPIC:
                EpicTask epicTask = (EpicTask) task;
                List<SubTask> subTasks = epicTask.getSubTasks();
                for (SubTask subtask : subTasks) {
                    taskById.remove(subtask.getId());
                    historyManager.remove(subtask.getId());
                }
                taskById.remove(task.getId());
                historyManager.remove(task.getId());
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
    public void createTask(Task task) throws TaskTimeOverlapException {
        switch (task.getTypeTask()) {
            case SUB:
                if (!isNoOverlap(task)) throw new TaskTimeOverlapException(overlapError);
                task.setId(getNextFreeId());
                taskById.put(task.getId(), task);
                SubTask subTask = (SubTask) task;
                EpicTask epicTask = (EpicTask) taskById.get(subTask.getEpicId());
                epicTask.modifySubTask(subTask);
                addByPriority(task);
                break;
            case REG:
                if (!isNoOverlap(task)) throw new TaskTimeOverlapException(overlapError);
                task.setId(getNextFreeId());
                taskById.put(task.getId(), task);
                addByPriority(task);
                break;
            default:
                task.setId(getNextFreeId());
                taskById.put(task.getId(), task);
                break;
        }
    }

    @Override
    public void updateTask(Task task) throws TaskTimeOverlapException {

        switch (task.getTypeTask()) {
            case SUB:
                if (!isNoOverlap(task)) throw new TaskTimeOverlapException(overlapError);
                taskById.put(task.getId(), task);
                prioritizedTasks.remove(task.getId());
                SubTask subTask = (SubTask) task;
                EpicTask epicTask = (EpicTask) taskById.get(subTask.getEpicId());
                epicTask.modifySubTask(subTask);
                addByPriority(task);
                break;
            case REG:
                if (!isNoOverlap(task)) throw new TaskTimeOverlapException(overlapError);
                taskById.put(task.getId(), task);
                prioritizedTasks.remove(task.getId());
                addByPriority(task);
            default:
                taskById.put(task.getId(), task);
                break;
        }
    }

    private Integer getNextFreeId() {
        return taskIdGenerator++;
    }

    public void setNextFreeId(Integer id) {
        taskIdGenerator = id;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private boolean isNoOverlap(Task task) { // Проверка на пересечение по времени задачи и уже имеющихся приоритезированных задач
        LocalDateTime start = task.getStartTime();
        LocalDateTime end = task.getEndTime();
        if (start.isEqual(NO_TIME))
            return true; // Задачи без времени не попадают в проверку и не будут учтены при приоритезации
        // Для каждой приоритезированной задачи проверяем попадает ли новая задача во временные рамки
        return prioritizedTasks.stream().allMatch(taskPrior -> taskPrior.getStartTime().isAfter(end) || taskPrior.getStartTime().isEqual(end) || taskPrior.getEndTime().isBefore(start) || taskPrior.getEndTime().isEqual(start));
    }

    public List<Task> getPrioritizedTasks() {
        return List.copyOf(prioritizedTasks);
    }

    protected void addByPriority(Task task) {
        // Задачи без времени не попадают в проверку и не будут учтены при приоритезации
        if (!task.getStartTime().isEqual(NO_TIME)) prioritizedTasks.add(task);
    }

    public void printTasks() {
        for (Task task : taskById.values()) {
            System.out.println(task);
        }
    }

    public void printPrioritizedTasks() {
        for (Task task : prioritizedTasks) {
            System.out.println(task);
        }
    }
}