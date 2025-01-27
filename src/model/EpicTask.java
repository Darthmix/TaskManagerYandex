package model;

import java.util.ArrayList;
import java.util.List;

public class EpicTask extends Task {
    private final List<SubTask> subTasks;

    public EpicTask(String name, String description) {
        super(name, description, StatusTask.NEW);
        this.subTasks = new ArrayList<>();
    }

    public List<SubTask> getSubTasks() {
        return this.subTasks;
    }

    public void setStatus(StatusTask statusTask) {
        this.statusTask = statusTask;
    }

    @Override
    public TypeTask getTypeTask() {
        return TypeTask.EPIC;
    }

    @Override
    public StatusTask getStatusTask() {
        return statusTask;
    }

    public void removeSubTask(SubTask subTask) {
        this.subTasks.remove(subTask);
        this.statusTask = calcStatus();
    }

    public void modifySubTask(SubTask subTask) { // Добавляем новую подзадачу если её не было, или изменяем
        boolean findSubTask = false;
        for (int i = 0; i < this.subTasks.size(); i++) {
            if (this.subTasks.get(i).hashCode() == subTask.hashCode()) {
                this.subTasks.set(i, subTask);
                findSubTask = true;
            }
        }
        if (!findSubTask) {
            this.subTasks.add(subTask);
        }
        this.statusTask = calcStatus();
    }

    private boolean isAllSubTasksByStatusTask(StatusTask statusTask) {
        for (SubTask subTask : this.subTasks) {
            if (!subTask.getStatusTask().equals(statusTask)) {
                return false;
            }
        }
        return true;
    }

    public StatusTask calcStatus() {
        if (this.subTasks.isEmpty() || isAllSubTasksByStatusTask(StatusTask.NEW)) {
            return StatusTask.NEW;
        } else if (isAllSubTasksByStatusTask(StatusTask.DONE)) {
            return StatusTask.DONE;
        } else {
            return StatusTask.IN_PROGRESS;
        }
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s",
                this.getId(),
                this.getTypeTask(),
                this.getName(),
                this.getStatusTask(),
                this.getDescription());
    }
}