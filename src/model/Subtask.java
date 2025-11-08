package model;

import enums.Status;
import enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int id, Status status, int epicId) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, Status status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(name, description, status);
        this.epicId = epicId;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Subtask(String name, String description, int id, Status status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(name, description, id, status);
        this.epicId = epicId;
        this.duration = duration;
        this.startTime = startTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                ", epicId=" + epicId +
                '}';
    }
}