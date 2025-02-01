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
    protected EpicTask epicTask;
    protected SubTask subTask1;
    protected SubTask subTask2;
    protected LocalDateTime startTime;

    protected abstract T createTestManager();

    @BeforeEach
    void initialize() {
        startTime = LocalDateTime.now();
        taskManager = createTestManager();
        singleTask = new SingleTask("CommonTask1", "Common task 1");
        taskManager.createTask(singleTask);
        epicTask = new EpicTask("EpicTask1", "Epic task 1");
        taskManager.createTask(epicTask);
        subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask.getId());
        taskManager.createTask(subTask1);
        subTask2 = new SubTask("SubTask2", "Subtask 2", epicTask.getId());
        taskManager.createTask(subTask2);
    }

    @Test
    void checkCreateDifferentTypeTasks() {
        assertNotNull(taskManager.getSingleTasks(), "Обычные задачи не создаются");
        assertNotNull(taskManager.getEpicTasks(), "Эпики задачи  не создаются");
        assertNotNull(taskManager.getSubTasks(), "Подзадачи задачи  не создаются");
    }

    @Test
    public void tasksEquality() {

        SingleTask singleTaskNew = new SingleTask("CommonTask1", "Common task 1", startTime, 15);
        taskManager.createTask(singleTaskNew);

        assertNotNull(singleTaskNew, "Задача не найдена!!!!");
        assertEquals(taskManager.getSingleTasks().get(1), taskManager.getTaskById(4), "Задачи не совпадают!!!");
    }

    @Test
    void checkDeleteAllTasks() {
        taskManager.clearSingleTasks();
        assertTrue(taskManager.getSingleTasks().isEmpty(), "Обычные задачи не удаляются");

        taskManager.clearSubTasks();
        assertTrue(taskManager.getSubTasks().isEmpty(), "Подзадачи не удаляются");

        taskManager.clearEpicTasks();
        assertTrue(taskManager.getEpicTasks().isEmpty(), "Эпики не удаляются");
    }

    @Test
    void checkEpicStatusWithNewStatus() {
        assertEquals(StatusTask.NEW, epicTask.getStatusTask(), "Статус эпика не верный");
    }

    @Test
    void checkEpicStatusWithDoneStatus() {
        subTask1.setStatus(StatusTask.DONE);
        taskManager.updateTask(subTask1);
        subTask2.setStatus(StatusTask.DONE);
        taskManager.updateTask(subTask2);
        assertEquals(StatusTask.DONE, epicTask.getStatusTask(), "Статус эпика не верный");
    }

    @Test
    void checkEpicStatusWithInProgressStatus() {
        subTask1.setStatus(StatusTask.IN_PROGRESS);
        taskManager.updateTask(subTask1);
        subTask2.setStatus(StatusTask.IN_PROGRESS);
        taskManager.updateTask(subTask2);
        assertEquals(StatusTask.IN_PROGRESS, epicTask.getStatusTask(), "Статус эпика не верный");
    }

    @Test
    void checkEpicStatusWithNewAndDoneStatus() {
        subTask1.setStatus(StatusTask.NEW);
        taskManager.updateTask(subTask1);
        subTask2.setStatus(StatusTask.DONE);
        taskManager.updateTask(subTask2);
        assertEquals(StatusTask.IN_PROGRESS, epicTask.getStatusTask(), "Статус эпика не верный");
    }

    @Test
    void checkEpicTime() {
        subTask1 = new SubTask("SubTask1", "Subtask 1", startTime, 30, epicTask.getId());
        taskManager.createTask(subTask1);
        subTask2 = new SubTask("SubTask2", "Subtask 2", startTime.plusHours(1), 30, epicTask.getId());
        taskManager.createTask(subTask2);

        assertEquals(startTime, taskManager.getTaskById(1).getStartTime(), "Стартовое время эпика не верно");
        assertEquals(subTask2.getEndTime(), taskManager.getTaskById(1).getEndTime(), "Финальное время эпика не верно");

        Duration duration = subTask1.getDuration().plus(subTask2.getDuration());
        assertEquals(duration, taskManager.getTaskById(1).getDuration(), "Длительность эпике не совпадает с длительностью подзадач");
    }

    @Test
    void checkTimeOverlap() {
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
