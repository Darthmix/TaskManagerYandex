package model;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

public class EpicTask extends Task {
    private final List<SubTask> subTasks;
    private LocalDateTime endTime;

    private static final LocalDateTime NO_TIME = LocalDateTime.of(1, 1, 1, 0, 0);

    public EpicTask(String name, String description) {
        super(name, description, StatusTask.NEW);
        this.subTasks = new ArrayList<>();
        this.endTime = LocalDateTime.of(1, 1, 1, 0, 0);
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
        subTasks.remove(subTask);
        statusTask = calcStatus();
        calcTime();
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
        calcTime();
    }

    private boolean isAllSubTasksByStatusTask(StatusTask statusTask) {
        return subTasks.stream().allMatch(subTask -> subTask.getStatusTask().equals(statusTask));
    }

    public void calcTime() {
        if (this.subTasks.isEmpty()) {
            setTimeFields(NO_TIME, NO_TIME, Duration.ofMinutes(0));
            return;
        }
        LocalDateTime startTime = subTasks.stream().map(subTask -> subTask.getStartTime()).filter(time -> !time.isEqual(NO_TIME)).min(LocalDateTime::compareTo).orElse(NO_TIME);
        LocalDateTime endTime = subTasks.stream().map(subTask -> subTask.getEndTime()).filter(time -> !time.isEqual(NO_TIME)).max(LocalDateTime::compareTo).orElse(NO_TIME);
        Duration duration = subTasks.stream().map(subTask -> subTask.getDuration()).reduce(Duration.ZERO, Duration::plus);
        setTimeFields(startTime, endTime, duration);
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

    private void setTimeFields(LocalDateTime startTime, LocalDateTime endTime, Duration duration) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s", this.getId(), this.getTypeTask(), this.getName(), this.getStatusTask(), this.getDescription());
    }
}