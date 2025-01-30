import model.EpicTask;
import model.SingleTask;
import model.SubTask;
import service.FileBackedTaskManager;
import service.Managers;

public class Main {
    public static void main(String[] args) {
        FileBackedTaskManager fileBackedTaskManager = (FileBackedTaskManager) Managers.getDefaultFileBackedTaskManager();

        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1");
        fileBackedTaskManager.createTask(singleTask1);

        SingleTask singleTask2 = new SingleTask("CommonTask2", "Common task 2");
        fileBackedTaskManager.createTask(singleTask2);

        EpicTask epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
        fileBackedTaskManager.createTask(epicTask1);

        SubTask subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask1.getId());
        fileBackedTaskManager.createTask(subTask1);
        SubTask subTask2 = new SubTask("SubTask2", "Subtask 2", epicTask1.getId());
        fileBackedTaskManager.createTask(subTask2);

        EpicTask epicTask2 = new EpicTask("EpicTask2", "Epic task 2");
        fileBackedTaskManager.createTask(epicTask2);

        SubTask subTask3 = new SubTask("SubTask3", "Subtask 3", epicTask2.getId());
        fileBackedTaskManager.createTask(subTask3);

        System.out.println("Созданные объекты");
        System.out.println(fileBackedTaskManager.getSingleTasks());
        System.out.println(fileBackedTaskManager.getEpicTasks());
        System.out.println(fileBackedTaskManager.getSubTasks());
    }
}