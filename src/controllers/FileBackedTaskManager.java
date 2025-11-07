package controllers;

import enums.Status;
import enums.TaskType;
import exceptions.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic,duration,startTime");
            writer.newLine();

            for (Task task : getAllTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }

            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private String toString(Task task) {
        TaskType type = task.getType();

        String durationStr = task.getDuration() != null ?
                String.valueOf(task.getDuration().toMinutes()) : "";
        String startTimeStr = task.getStartTime() != null ?
                task.getStartTime().toString() : "";

        switch (type) {
            case EPIC:
                return String.format("%d,EPIC,%s,%s,%s,,%s,%s",
                        task.getId(), task.getName(), task.getStatus(),
                        task.getDescription(), durationStr, startTimeStr);
            case SUBTASK:
                Subtask subtask = (Subtask) task;
                return String.format("%d,SUBTASK,%s,%s,%s,%d,%s,%s",
                        subtask.getId(), subtask.getName(), subtask.getStatus(),
                        subtask.getDescription(), subtask.getEpicId(),
                        durationStr, startTimeStr);
            case TASK:
            default:
                return String.format("%d,TASK,%s,%s,%s,,%s,%s",
                        task.getId(), task.getName(), task.getStatus(),
                        task.getDescription(), durationStr, startTimeStr);
        }
    }

    private Task fromString(String value) {
        String[] fields = value.split(",", -1);
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        Duration duration = fields.length > 6 && !fields[6].isEmpty() ?
                Duration.ofMinutes(Long.parseLong(fields[6])) : null;
        LocalDateTime startTime = fields.length > 7 && !fields[7].isEmpty() ?
                LocalDateTime.parse(fields[7]) : null;

        switch (type) {
            case TASK:
                return new Task(name, description, id, status, duration, startTime);
            case EPIC:
                Epic epic = new Epic(name, description, id, status);
                return epic;
            case SUBTASK:
                int epicId = fields.length > 5 && !fields[5].isEmpty() ?
                        Integer.parseInt(fields[5]) : 0;
                return new Subtask(name, description, id, status, epicId, duration, startTime);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        if (!file.exists()) {
            return manager;
        }

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                Task task = manager.fromString(line);
                TaskType type = task.getType();

                switch (type) {
                    case EPIC:
                        manager.epics.put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        manager.subtasks.put(task.getId(), (Subtask) task);
                        break;
                    case TASK:
                    default:
                        manager.tasks.put(task.getId(), task);
                        break;
                }

                if (task.getId() >= manager.nextId) {
                    manager.nextId = task.getId() + 1;
                }

                if (task.getStartTime() != null) {
                    manager.prioritizedTasks.add(task);
                }
            }

            for (Subtask subtask : manager.subtasks.values()) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                }
            }

            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic.getId());
                manager.updateEpicTime(epic.getId());
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        return manager;
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}
