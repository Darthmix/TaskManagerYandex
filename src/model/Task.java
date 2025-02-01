package model;

import java.util.Objects;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class Task {
    private int id;
    private final String name;
    private final String description;
    protected StatusTask statusTask;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description, StatusTask statusTask) { // Конструктор с установкой статуса
        this.name = name;
        this.description = description;
        this.statusTask = statusTask;
        this.duration = Duration.ZERO;
        this.startTime = LocalDateTime.of(1, 1, 1, 0, 0);
    }

    public Task(String name, String description, StatusTask statusTask, LocalDateTime startTime, int durationInMinutes) {
        this.name = name;
        this.description = description;
        this.statusTask = statusTask;
        this.duration = Duration.ofMinutes(durationInMinutes);
        this.startTime = startTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public StatusTask getStatusTask() {
        return statusTask;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() { // Сгенерированный Хеш
        return Objects.hash(id);
    }

    public abstract TypeTask getTypeTask(); // Определение типа задачи

    @Override
    public abstract String toString();
}