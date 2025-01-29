package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTaskTest {
    private final Task epicTask1 = new EpicTask("EpicTask1", "Epic task 1");
    private final Task epicTask2 = new EpicTask("EpicTask2", "Epic task 2");

    @Test
    void tasksEqualSameIdTest() {
        int id = 123123;
        epicTask1.setId(id);
        epicTask2.setId(id);
        assertEquals(epicTask1, epicTask2, "Задачи с одним ID не равны");
        epicTask2.setId(id + 1);
        assertNotEquals(epicTask1, epicTask2, "Задачи с разными ID равны");
    }

}