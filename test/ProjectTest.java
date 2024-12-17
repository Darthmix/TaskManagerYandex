import model.*;
import service.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ProjectTest {

    @Test
    public void checkHistorySequence() {
        InMemoryTaskManager taskManager = (InMemoryTaskManager) Managers.getDefaultTaskManager();

        // Создаём 2 обычные задачи, сохраняем их
        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1");
        taskManager.createTask(singleTask1);

        SingleTask singleTask2 = new SingleTask("CommonTask2", "Common task 2");
        taskManager.createTask(singleTask2);

        Assertions.assertNotNull(taskManager.getSingleTasks(), "Задачи не найдены!!!!");

        SingleTask singleTaskChanged = (SingleTask) taskManager.getTaskById(0);
        singleTaskChanged.setStatus(StatusTask.IN_PROGRESS);
        taskManager.updateTask(singleTaskChanged);

        Assertions.assertNotNull(taskManager.getHistory(), "История не сформирована");
        Assertions.assertEquals(taskManager.getHistory().get(1), singleTaskChanged, "Не правильная последовательность в истории");
    }

    @Test
    public void checkHistoryRemove() {
        InMemoryTaskManager taskManager = (InMemoryTaskManager) Managers.getDefaultTaskManager();

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

        List<Task> epicTasks = taskManager.getEpicTasks();

        List<Task> historyTasksFirstOccur = taskManager.getHistory();

        taskManager.removeTask(0); // Удаляем первый эпик со всеми подзадачами
        epicTasks = taskManager.getEpicTasks();
        List<Task> historyTasksSecondOccur = taskManager.getHistory();
        Assertions.assertNotEquals(historyTasksFirstOccur, historyTasksSecondOccur, "Задачи не удаляются из истории!!!");
    }

    @Test
    public void tasksEquality() {
        InMemoryTaskManager taskManager = (InMemoryTaskManager) Managers.getDefaultTaskManager();

        // Создаём 2 обычные задачи, сохраняем их
        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1");
        taskManager.createTask(singleTask1);

        SingleTask singleTask2 = new SingleTask("CommonTask2", "Common task 2");
        taskManager.createTask(singleTask2);

        List<Task> singleTasks = taskManager.getSingleTasks();
        Task singleTaskTmp = taskManager.getTaskById(0);
        Assertions.assertNotNull(singleTask1, "Задача не найдена!!!!");
        Assertions.assertEquals(singleTasks.get(0), singleTaskTmp, "Задачи не совпадают!!!");
    }

    @Test
    public void checkManagersInitialization() {

        InMemoryHistoryManager historyManagerLocal = (InMemoryHistoryManager) Managers.getDefaultHistory();
        Assertions.assertNotNull(historyManagerLocal, "Менеджер истории не проинициализирован!!!");

        InMemoryTaskManager taskManagerLocal = (InMemoryTaskManager) Managers.getDefaultTaskManager();
        Assertions.assertNotNull(taskManagerLocal, "Менеджер задач не проинициализирован!!!");
    }

    @Test
    public void checkCreateDifferentTypeTasks() {
        InMemoryTaskManager taskManager = (InMemoryTaskManager) Managers.getDefaultTaskManager();

        // Создаём 2 обычные задачи, сохраняем их
        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1");
        taskManager.createTask(singleTask1);

        SingleTask singleTask2 = new SingleTask("CommonTask2", "Common task 2");
        taskManager.createTask(singleTask2);

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

        List<Task> singleTasks = taskManager.getSingleTasks();
        Assertions.assertNotNull(singleTasks, "Одиночные задачи не создаются!!!");
        List<Task> epicTasks = taskManager.getEpicTasks();
        Assertions.assertNotNull(epicTasks, "Эпики не создаются!!!");
        List<Task> subTasks = taskManager.getSubTasks();
        Assertions.assertNotNull(subTasks, "Подзадачи не создаются!!!");
    }
}
