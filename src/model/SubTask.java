package model;

public class SubTask extends Task {
    private final Integer epicTaskId;

    public SubTask(String name, String description, Integer epicTaskId) {
        super(name, description, StatusTask.NEW);
        this.epicTaskId = epicTaskId;
    }

    public SubTask(String name, String description, Integer epicTaskId, StatusTask statusTask) {
        this(name, description, epicTaskId);
        this.statusTask = statusTask;
    }


    public void removeFromEpic(EpicTask epicTask) {
        epicTask.removeSubTask(this);
    }

    public void setStatus(StatusTask statusTask) {
        this.statusTask = statusTask;
    }

    public int getEpicId() {
        return epicTaskId;
    }

    @Override
    public StatusTask getStatusTask() {
        return statusTask;
    }

    @Override
    public TypeTask getTypeTask() {
        return TypeTask.SUB;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d",
                this.getId(),
                this.getTypeTask(),
                this.getName(),
                this.getStatusTask(),
                this.getDescription(),
                this.getEpicId());
    }
}