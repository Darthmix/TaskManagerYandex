package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class SingleTask extends Task {

    public SingleTask(String name, String description) {
        super(name, description, StatusTask.NEW);
    }

    public SingleTask(String name, String description, StatusTask statusTask) {
        super(name, description, statusTask);
    }

    public SingleTask(String name, String description, LocalDateTime startTime, int durationInMinutes) {
        super(name, description, StatusTask.NEW, startTime, durationInMinutes);
    }

    public SingleTask(String name, String description, StatusTask statusTask, LocalDateTime startTime, int durationInMinutes) {
        super(name, description, statusTask, startTime, durationInMinutes);
    }

    @Override
    public StatusTask getStatusTask() {
        return statusTask;
    }

    public void setStatus(StatusTask statusTask) {
        this.statusTask = statusTask;
    }

    @Override
    public TypeTask getTypeTask() {
        return TypeTask.REG;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%s,%s", this.getId(), this.getTypeTask(), this.getName(), this.getStatusTask(), this.getDescription(), startTime.format(DateTimeFormat.DATE_TIME_FORMAT), duration.toMinutes());
    }
}