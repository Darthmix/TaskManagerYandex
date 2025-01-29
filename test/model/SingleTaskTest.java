package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleTaskTest {
    @Test
    void tasksEqualSameIdTest() {
        SingleTask singleTask1 = new SingleTask("CommonTask1", "Common task 1");
        SingleTask singleTask2 = new SingleTask("CommonTask2", "Common task 2");
        int id = 123132;
        singleTask1.setId(id);
        singleTask2.setId(id);

        assertEquals(singleTask1, singleTask2, "Задачи с одним Id не равны");
        singleTask2.setId(id + 1);
        assertNotEquals(singleTask1, singleTask2, "Задачи с разными Id равны");
    }
}