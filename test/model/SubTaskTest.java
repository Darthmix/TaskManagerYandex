package model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {
    private final EpicTask epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
    private final SubTask subTask1 = new SubTask("SubTask1", "Subtask 1", epicTask1.getId());
    private final SubTask subTask2= new SubTask("SubTask2", "Subtask 2", epicTask1.getId());

    @Test
    void tasksEqualSameIdTest() {
        int id = 123123;
        subTask1.setId(id);
        subTask2.setId(id);
        assertEquals(subTask1, subTask2, "Задачи с одним ID не равны");
        subTask2.setId(id + 1);
        assertNotEquals(subTask1, subTask2, "Задачи с разными ID равны");
    }

    @Test
    void removeSubTaskFromEpicTest(){
        List<SubTask> subTasks = new ArrayList<>();

        subTask1.setId(1);
        subTask2.setId(2);

        epicTask1.modifySubTask(subTask1);
        epicTask1.modifySubTask(subTask2);
        subTask1.removeFromEpic(epicTask1);
        subTasks = epicTask1.getSubTasks();

        assertFalse(subTasks.contains(subTask1), "Удаление подзадачи не работает");
    }
}