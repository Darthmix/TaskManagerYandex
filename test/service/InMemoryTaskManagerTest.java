package service;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    public InMemoryTaskManager createTestManager() {
        return new InMemoryTaskManager();
    }
}
