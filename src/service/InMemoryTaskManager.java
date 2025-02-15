package service;

import model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
        return taskById.values()
                       .stream()
                       .filter(task -> task.getTypeTask().equals(typeTask))
                       .peek(historyManager::add)
                       .collect(Collectors.toList());
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
                removePrioritizedTasks(task);
                break;
            case SUB:
                SubTask subTask = (SubTask) task;
                subTask.removeFromEpic((EpicTask) taskById.get(subTask.getEpicId()));
                taskById.remove(task.getId());
                historyManager.remove(task.getId());
                break;
            case EPIC:
                EpicTask epicTask = (EpicTask) task;
                epicTask.getSubTasks().stream().forEach(subTaskTmp -> {
                    taskById.remove(subTaskTmp.getId());
                    historyManager.remove(subTaskTmp.getId());
                });
                taskById.remove(task.getId());
                historyManager.remove(task.getId());
                removePrioritizedTasks(task);
                break;
        }
    }

    private void clearByType(TypeTask typeTask) {
        getTasksByType(typeTask).stream().forEach(task -> removeTask(task.getId()));
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
                removePrioritizedTasks(task);
                SubTask subTask = (SubTask) task;
                EpicTask epicTask = (EpicTask) taskById.get(subTask.getEpicId());
                epicTask.modifySubTask(subTask);
                addByPriority(task);
                break;
            case REG:
                if (!isNoOverlap(task)) throw new TaskTimeOverlapException(overlapError);
                taskById.put(task.getId(), task);
                removePrioritizedTasks(task);
                addByPriority(task);
            default:
                taskById.put(task.getId(), task);
                break;
        }
    }

    private void removePrioritizedTasks(Task task) {
        if (prioritizedTasks.contains(task)) {
            prioritizedTasks.stream()
                            .filter(taskTmp -> taskTmp.getId() != task.getId())
                            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Task::getStartTime))));
            ;
        }
    }


    private Integer getNextFreeId() {
        return ++taskIdGenerator;
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
        return prioritizedTasks.stream()
                               .allMatch(taskPrior -> (taskPrior.getStartTime().isEqual(start) &&
                                                       taskPrior.getEndTime().isEqual(end)) ||
                                                      taskPrior.getStartTime().isAfter(end) ||
                                                      taskPrior.getStartTime().isEqual(end) ||
                                                      taskPrior.getEndTime().isBefore(start) ||
                                                      taskPrior.getEndTime().isEqual(start));

    }

    public List<Task> getPrioritizedTasks() {
        return List.copyOf(prioritizedTasks);
    }

    protected void addByPriority(Task task) {
        // Задачи без времени не попадают в проверку и не будут учтены при приоритезации
        if (!task.getStartTime().isEqual(NO_TIME)) prioritizedTasks.add(task);
    }

}