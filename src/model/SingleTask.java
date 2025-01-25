package model;

public class SingleTask extends Task {

    public SingleTask(String name, String description) {
        super(name, description, StatusTask.NEW);
    }

    public SingleTask(String name, String description, StatusTask statusTask) {
        super(name, description, statusTask);
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
        return String.format("%d,%s,%s,%s,%s",
                this.getId(),
                this.getTypeTask(),
                this.getName(),
                this.getStatusTask(),
                this.getDescription());
    }
}