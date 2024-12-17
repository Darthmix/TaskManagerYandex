package model;

import java.util.Objects;

public abstract class Task {
    private int id;
    private final String name;
    private final String description;
    protected StatusTask statusTask;

    public Task(String name, String description, StatusTask statusTask) { // Конструктор с установкой статуса
        this.name = name;
        this.description = description;
        this.statusTask = statusTask;
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