package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void ManagerReturnTaskManagerTest() {
        assertInstanceOf(TaskManager.class, Managers.getDefaultTaskManager());
    }

    @Test
    void ManagerReturnHistoryManagerTest() {
        assertInstanceOf(HistoryManager.class, Managers.getDefaultHistory());
    }

    @Test
    void ManagerReturnFileBackedTaskManagerTest() {
        assertInstanceOf(FileBackedTaskManager.class, Managers.getDefaultFileBackedTaskManager());
    }
}