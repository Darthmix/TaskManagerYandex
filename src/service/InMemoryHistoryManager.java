package service;

import model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private static final byte HISTORY_LIST_MAX_SIZE = 10;
    private final LinkedList<Task> historyTasks; // История изменений

    public InMemoryHistoryManager() {
        this.historyTasks = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (historyTasks.size() >= HISTORY_LIST_MAX_SIZE) {
            historyTasks.removeFirst();
        }
        historyTasks.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(historyTasks);
    }
}
