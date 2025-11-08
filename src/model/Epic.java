package model;

import enums.Status;
import enums.TaskType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    public Epic(String name, String description, int id, Status status) {
        super(name, description, id, status);
        this.subtaskIds = new ArrayList<>();
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int subtaskId) {
        if (subtaskIds == null) {
            subtaskIds = new ArrayList<>();
        }
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        if (subtaskIds != null) {
            subtaskIds.remove(Integer.valueOf(subtaskId));
        }
    }

    public void clearSubtaskIds() {
        if (subtaskIds != null) {
            subtaskIds.clear();
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", subtaskIds=" + (subtaskIds != null ? subtaskIds : "[]") +
                '}';
    }
}