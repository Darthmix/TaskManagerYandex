package service;

import model.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;
    protected SingleTask singleTask;
    protected EpicTask epicTask1;
    protected SubTask subTask1;
    protected SubTask subTask2;

    protected abstract T createTestManager();

    @BeforeEach
    void initialize() {
        taskManager = createTestManager();
    }

    @Test
    void checkCreateDifferentTypeTasks() {
        singleTask = new SingleTask("CommonTask1", "Common task 1");
        taskManager.createTask(singleTask);
        epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
        taskManager.createTask(epicTask1);
        subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask1.getId());
        taskManager.createTask(subTask1);
        subTask2 = new SubTask("SubTask2", "Subtask 2", epicTask1.getId());
        taskManager.createTask(subTask2);

        final List<Task> singleTasks = taskManager.getSingleTasks();
        assertNotNull(singleTasks, "Обычные задачи не создаются");

        final List<Task> epicTasks = taskManager.getEpicTasks();
        assertNotNull(epicTasks, "Эпики задачи  не создаются");

        final List<Task> subTasks = taskManager.getSubTasks();
        assertNotNull(epicTasks, "Подзадачи задачи  не создаются");
    }

    @Test
    public void tasksEquality() {

        // Создаём 2 обычные задачи, сохраняем их
        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1", LocalDateTime.now(), 15);
        taskManager.createTask(singleTask1);

        SingleTask singleTask2 = new SingleTask("CommonTask2", "Common task 2");
        taskManager.createTask(singleTask2);

        List<Task> singleTasks = taskManager.getSingleTasks();
        Task singleTaskTmp = taskManager.getTaskById(0);
        assertNotNull(singleTask1, "Задача не найдена!!!!");
        assertEquals(singleTasks.get(0), singleTaskTmp, "Задачи не совпадают!!!");
    }

    @Test
    void checkDeleteAllTasks() {
        singleTask = new SingleTask("CommonTask1", "Common task 1");
        taskManager.createTask(singleTask);
        epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
        taskManager.createTask(epicTask1);
        subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask1.getId());
        taskManager.createTask(subTask1);
        subTask2 = new SubTask("SubTask2", "Subtask 2", epicTask1.getId());
        taskManager.createTask(subTask2);

        taskManager.clearSingleTasks();
        assertTrue(taskManager.getSingleTasks().isEmpty(), "Обычные задачи не удаляются");

        taskManager.clearSubTasks();
        assertTrue(taskManager.getSubTasks().isEmpty(), "Подзадачи не удаляются");

        taskManager.clearEpicTasks();
        assertTrue(taskManager.getEpicTasks().isEmpty(), "Эпики не удаляются");
    }

    @Test
    void checkEpicStatusWithNewStatus() {
        epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
        taskManager.createTask(epicTask1);
        subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask1.getId());
        taskManager.createTask(subTask1);
        subTask2 = new SubTask("SubTask2", "Subtask 2", epicTask1.getId());
        taskManager.createTask(subTask2);
        assertEquals(StatusTask.NEW, epicTask1.getStatusTask(), "Статус эпика не верный");
    }

    @Test
    void checkEpicStatusWithDoneStatus() {
        epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
        taskManager.createTask(epicTask1);
        subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask1.getId(), StatusTask.DONE);
        taskManager.createTask(subTask1);
        subTask2 = new SubTask("SubTask2", "Subtask 2", epicTask1.getId(), StatusTask.DONE);
        taskManager.createTask(subTask2);
        assertEquals(StatusTask.DONE, epicTask1.getStatusTask(), "Статус эпика не верный");
    }

    @Test
    void checkEpicStatusWithInProgressStatus() {
        epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
        taskManager.createTask(epicTask1);
        subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask1.getId(), StatusTask.IN_PROGRESS);
        taskManager.createTask(subTask1);
        subTask2 = new SubTask("SubTask2", "Subtask 2", epicTask1.getId(), StatusTask.IN_PROGRESS);
        taskManager.createTask(subTask2);
        assertEquals(StatusTask.IN_PROGRESS, epicTask1.getStatusTask(), "Статус эпика не верный");
    }

    @Test
    void checkEpicStatusWithNewAndDoneStatus() {
        epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
        taskManager.createTask(epicTask1);
        subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask1.getId(), StatusTask.NEW);
        taskManager.createTask(subTask1);
        subTask2 = new SubTask("SubTask2", "Subtask 2", epicTask1.getId(), StatusTask.DONE);
        taskManager.createTask(subTask2);
        assertEquals(StatusTask.IN_PROGRESS, epicTask1.getStatusTask(), "Статус эпика не верный");
    }

    @Test
    void checkEpicTime() {
        LocalDateTime startTime = LocalDateTime.now();
        epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
        taskManager.createTask(epicTask1);
        subTask1 = new SubTask("SubTask1", "Subtask 1", startTime, 30, epicTask1.getId());
        taskManager.createTask(subTask1);
        subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(1), 30, epicTask1.getId());
        taskManager.createTask(subTask2);

        assertEquals(startTime, taskManager.getTaskById(0).getStartTime(), "Стартовое время эпика не верно");
        assertEquals(subTask2.getEndTime(), taskManager.getTaskById(0).getEndTime(), "Финальное время эпика не верно");

        Duration duration = subTask1.getDuration().plus(subTask2.getDuration());
        assertEquals(duration, taskManager.getTaskById(0).getDuration(), "Длительность эпике не совпадает с длительностью подзадач");
    }

    @Test
    void checkTimeOverlap() {
        LocalDateTime startTime = LocalDateTime.now();
        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1", startTime, 30);
        taskManager.createTask(singleTask1);
        SingleTask singleTask2 = new SingleTask("CommonTask2", "Common task 2", startTime.plus(Duration.ofMinutes(15)), 30);
        SingleTask singleTask3 = new SingleTask("CommonTask3", "Common task 3", startTime.minus(Duration.ofMinutes(15)), 30);
        SingleTask singleTask4 = new SingleTask("CommonTask3", "Common task 3", startTime.minus(Duration.ofMinutes(35)), 30);

        assertThrows(TaskTimeOverlapException.class, () -> taskManager.createTask(singleTask2), "Пересечение по времени не выявлено");
        assertThrows(TaskTimeOverlapException.class, () -> taskManager.createTask(singleTask3), "Пересечение по времени не выявлено");
        assertDoesNotThrow(() -> taskManager.createTask(singleTask4), "Пересечение по времени не выявлено");
    }

    @Test
    void checkTasksBySortedByPriority() {
        LocalDateTime startTime = LocalDateTime.now();
        SingleTask singleTaskNoTime = new SingleTask("CommonTask1", "Common task 1");
        taskManager.createTask(singleTaskNoTime);
        SingleTask singleTaskWithTime1 = new SingleTask("CommonTask2", "Common task 2", startTime, 30);
        taskManager.createTask(singleTaskWithTime1);
        SingleTask singleTaskWithTime2 = new SingleTask("CommonTask2", "Common task 2", startTime.plusHours(1), 30);
        taskManager.createTask(singleTaskWithTime2);
        SingleTask singleTaskWithTime3 = new SingleTask("CommonTask2", "Common task 2", startTime.minusHours(1), 30);
        taskManager.createTask(singleTaskWithTime3);
        assertFalse(taskManager.getPrioritizedTasks().contains(singleTaskNoTime), "Задача без времени содержится в приоритезированных задачах");
        assertEquals(singleTaskWithTime3, taskManager.getPrioritizedTasks().get(0), "Не правильно определена первая задача");
        assertEquals(singleTaskWithTime2, taskManager.getPrioritizedTasks().get(2), "Не правильно определена последняя задача");

    }

}
