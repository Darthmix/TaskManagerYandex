package service;

import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    public void initializeManagers() {
        taskManager = Managers.getDefaultTaskManager();
    }

    @Test
    public void checkHistorySequence() {

        // Создаём 2 обычные задачи, сохраняем их
        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1");
        taskManager.createTask(singleTask1);

        SingleTask singleTask2 = new SingleTask("CommonTask2", "Common task 2");
        taskManager.createTask(singleTask2);

        Assertions.assertNotNull(taskManager.getSingleTasks(), "Задачи не найдены!!!!");

        SingleTask singleTaskChanged = (SingleTask) taskManager.getTaskById(0);
        singleTaskChanged.setStatus(StatusTask.IN_PROGRESS);
        taskManager.updateTask(singleTaskChanged);

        assertNotNull(taskManager.getHistory(), "История не сформирована");
        assertEquals(taskManager.getHistory().get(1), singleTaskChanged, "Не правильная последовательность в истории");
    }

    @Test
    public void checkHistoryRemove() {

        // Создаём первый эпик с 2 подзадачами и сохраняем
        EpicTask epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
        taskManager.createTask(epicTask1);

        SubTask subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask1.getId());
        taskManager.createTask(subTask1);
        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", epicTask1.getId());
        taskManager.createTask(subTask2);

        // Создаём второй эпик с одной подзадачей и сохраняем
        EpicTask epicTask2 = new EpicTask("EpicTask2", "Epic task 2");
        taskManager.createTask(epicTask2);

        SubTask subTask3 = new SubTask("SubTask3", "Subtask 3", epicTask2.getId());
        taskManager.createTask(subTask3);

        taskManager.getEpicTasks();

        List<Task> historyTasksFirstOccur = taskManager.getHistory();

        taskManager.removeTask(0); // Удаляем первый эпик со всеми подзадачами
        taskManager.getEpicTasks();
        List<Task> historyTasksSecondOccur = taskManager.getHistory();
        assertNotEquals(historyTasksFirstOccur, historyTasksSecondOccur, "Задачи не удаляются из истории!!!");
    }

}