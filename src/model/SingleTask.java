package model;

public class SingleTask extends Task {

    public SingleTask(String name, String description) {
        super(name, description, StatusTask.NEW);
    }

    public SingleTask(String name, String description, StatusTask statusTask) {
        super(name, description, statusTask);
    }

    public StatusTask getStatus() {
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
        return "SingleTask{" + "id=" + this.getId() + ", name='" + this.getName() + '\'' + ", description='" + this.getDescription() + '\'' + ", statusTask=" + statusTask + '}' + '\n';
    }
}
