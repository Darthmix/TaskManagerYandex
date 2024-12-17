import model.*;
import service.*;

public class Main {
    public static void main(String[] args) {

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

        System.out.println("Созданные объекты");
        System.out.println(taskManager.getSingleTasks());
        System.out.println(taskManager.getEpicTasks());
        System.out.println(taskManager.getSubTasks());

        // Установил новые статусы для обычных задач
        SingleTask tmpSingleTask;

        // Для первой обычной задачи установим статус "В процессе"
        tmpSingleTask = (SingleTask) taskManager.getTaskById(0);
        tmpSingleTask.setStatus(StatusTask.IN_PROGRESS);
        taskManager.updateTask(tmpSingleTask);

        // Для первой обычной задачи установим статус "Готово"
        tmpSingleTask = (SingleTask) taskManager.getTaskById(1);
        tmpSingleTask.setStatus(StatusTask.DONE);
        taskManager.updateTask(tmpSingleTask);

        // Установил новые статусы для подзадач
        SubTask tmpSubTask;

        // Для подзадачи SubTask1 установим статус "В процессе"
        tmpSubTask = (SubTask) taskManager.getTaskById(3);
        tmpSubTask.setStatus(StatusTask.IN_PROGRESS);
        taskManager.updateTask(tmpSubTask);

        // Для подзадачи SubTask3 установим статус "Готово"
        tmpSubTask = (SubTask) taskManager.getTaskById(6);
        tmpSubTask.setStatus(StatusTask.DONE);
        taskManager.updateTask(tmpSubTask);


        System.out.println("После изменения статусов");
        System.out.println(taskManager.getSingleTasks());
        System.out.println(taskManager.getEpicTasks());
        System.out.println(taskManager.getSubTasks());

        // Получение списка задач в зависимости от типа
        System.out.println("Список обычных задач:");
        System.out.println(taskManager.getSingleTasks());
        System.out.println("Список эпик задач:");
        System.out.println(taskManager.getEpicTasks());
        System.out.println("Список подзадач задач:");
        System.out.println(taskManager.getSubTasks());

        // Проверка удаления
        System.out.println("Удаляем первую(0) обычную задачу");
        taskManager.removeTask(0);
        System.out.println(taskManager.getSingleTasks());
        System.out.println(taskManager.getEpicTasks());
        System.out.println(taskManager.getSubTasks());

        System.out.println("Удаляем подзадачу 1 эпика 1");
        taskManager.removeTask(3);
        System.out.println(taskManager.getSingleTasks());
        System.out.println(taskManager.getEpicTasks());
        System.out.println(taskManager.getSubTasks());

        System.out.println("Удаляем эпик 2");
        taskManager.removeTask(5);
        System.out.println(taskManager.getSingleTasks());
        System.out.println(taskManager.getEpicTasks());
        System.out.println(taskManager.getSubTasks());

        System.out.println("Удаляем обычные задачи");
        taskManager.clearSingleTasks();
        System.out.println(taskManager.getSingleTasks());
        System.out.println(taskManager.getEpicTasks());
        System.out.println(taskManager.getSubTasks());

        System.out.println("Удаляем эпики");
        taskManager.clearEpicTasks();
        System.out.println(taskManager.getSingleTasks());
        System.out.println(taskManager.getEpicTasks());
        System.out.println(taskManager.getSubTasks());

        System.out.println("Удаляем подзадачи");
        taskManager.clearSubTasks();
        System.out.println(taskManager.getSingleTasks());
        System.out.println(taskManager.getEpicTasks());
        System.out.println(taskManager.getSubTasks());

    }
}