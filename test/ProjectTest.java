import model.*;
import service.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ProjectTest {
    private static TaskManager taskManager;

    @BeforeAll
    public static void createInMemoryTaskManager() {

        taskManager = Managers.getDefaultTaskManager();

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
    }

    @Test
    public void checkHistory() {

        //Для проверки истории необходимо использовать локальные данные
        TaskManager taskManager1 = Managers.getDefaultTaskManager();

        // Создаём 2 обычные задачи, сохраняем их
        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1");
        taskManager1.createTask(singleTask1);

        SingleTask singleTask2 = new SingleTask("CommonTask2", "Common task 2");
        taskManager1.createTask(singleTask2);

        // Последние просмотренные пользователем задачи
        List<Task> singleTasks = taskManager1.getSingleTasks();

        // Изменяем статус первой задачи для проверки в истории
        SingleTask singleTaskChanged = (SingleTask) taskManager.getTaskById(0);
        Assertions.assertNotNull(singleTaskChanged, "Задача не найдена!!!!");
        singleTaskChanged.setStatus(StatusTask.IN_PROGRESS);
        taskManager1.updateTask(singleTaskChanged);

        // Последние просмотренные пользователем задачи
        singleTasks = taskManager1.getSingleTasks();

        List<Task> history = taskManager1.getHistory();

        // Поиск старого значения и нового
        boolean find_new = false;
        boolean find_in_progres = false;
        for (Task task : history) {
            if (task.equals(singleTaskChanged)) {
                if (task.getStatusTask().equals(StatusTask.NEW)) {
                    find_new = true;
                }
                if (task.getStatusTask().equals(StatusTask.IN_PROGRESS)) {
                    find_in_progres = true;
                }
            }
        }
        Assertions.assertTrue(find_new && find_in_progres, "История не сохраняется!!!");
    }

    @Test
    public void tasksEquality() {
        List<Task> singleTasks = taskManager.getSingleTasks();
        Task singleTask1 = taskManager.getTaskById(0);
        Assertions.assertNotNull(singleTask1, "Задача не найдена!!!!");
        Assertions.assertEquals(singleTasks.get(0), singleTask1, "Задачи не совпадают!!!");
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
        List<Task> singleTasks = taskManager.getSingleTasks();
        Assertions.assertNotNull(singleTasks, "Одиночные задачи не создаются!!!");
        List<Task> epicTasks = taskManager.getEpicTasks();
        Assertions.assertNotNull(epicTasks, "Эпики не создаются!!!");
        List<Task> subTasks = taskManager.getSubTasks();
        Assertions.assertNotNull(subTasks, "Подзадачи не создаются!!!");
    }
}
