package controllers;

import enums.Status;
import impl.HistoryManager;
import impl.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    protected final HistoryManager historyManager;
    protected final Set<Task> prioritizedTasks;
    protected int nextId;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.prioritizedTasks = new TreeSet<>(Comparator.comparing(
                Task::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        this.nextId = 1;
    }

    private int generateId() {
        return nextId++;
    }

    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    protected void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> epicSubtasks = getSubtasksByEpicId(epicId);

        if (epicSubtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(null);
            epic.setEndTime(null);
            return;
        }

        LocalDateTime startTime = epicSubtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        Duration duration = epicSubtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        // Рассчитываем endTime как максимальное время окончания подзадач
        LocalDateTime endTime = epicSubtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        epic.setStartTime(startTime);
        epic.setDuration(duration);
        epic.setEndTime(endTime);
    }

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritizedTasks(Task task) {
        prioritizedTasks.remove(task);
    }

    @Override
    public boolean hasTimeOverlap(Task task1, Task task2) {
        if (task1 == task2) return false;
        if (task1.getStartTime() == null || task2.getStartTime() == null ||
                task1.getEndTime() == null || task2.getEndTime() == null) {
            return false;
        }

        return task1.getStartTime().isBefore(task2.getEndTime()) &&
                task2.getStartTime().isBefore(task1.getEndTime());
    }

    @Override
    public boolean isTimeOverlappingWithExisting(Task task) {
        if (task.getStartTime() == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .filter(existingTask -> existingTask.getId() != task.getId())
                .anyMatch(existingTask -> hasTimeOverlap(task, existingTask));
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            removeFromPrioritizedTasks(task);
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Task createTask(Task task) {
        if (isTimeOverlappingWithExisting(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующей задачей");
        }

        task.setId(generateId());
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task existingTask = tasks.get(task.getId());
            removeFromPrioritizedTasks(existingTask);

            if (isTimeOverlappingWithExisting(task)) {
                addToPrioritizedTasks(existingTask);
                throw new IllegalArgumentException("Задача пересекается по времени с существующей задачей");
            }

            tasks.put(task.getId(), task);
            addToPrioritizedTasks(task);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPrioritizedTasks(task);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    removeFromPrioritizedTasks(subtask);
                    historyManager.remove(subtaskId);
                }
            }
        }
        epics.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(generateId());
        epic.setStatus(Status.NEW);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic existingEpic = epics.get(epic.getId());
        if (existingEpic != null) {
            List<Integer> subtaskIds = existingEpic.getSubtaskIds();
            epic.setSubtaskIds(new ArrayList<>(subtaskIds));
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic.getId());
            updateEpicTime(epic.getId());
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(id);
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    removeFromPrioritizedTasks(subtask);
                    historyManager.remove(subtaskId);
                }
            }
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic.getId());
            updateEpicTime(epic.getId());
        }
        for (Subtask subtask : subtasks.values()) {
            removeFromPrioritizedTasks(subtask);
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return null;
        }

        if (isTimeOverlappingWithExisting(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующей задачей");
        }

        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic.getId());
        updateEpicTime(epic.getId());
        addToPrioritizedTasks(subtask);
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask existingSubtask = subtasks.get(subtask.getId());
        if (existingSubtask != null) {
            removeFromPrioritizedTasks(existingSubtask);

            if (isTimeOverlappingWithExisting(subtask)) {
                addToPrioritizedTasks(existingSubtask);
                throw new IllegalArgumentException("Подзадача пересекается по времени с существующей задачей");
            }

            subtasks.put(subtask.getId(), subtask);
            addToPrioritizedTasks(subtask);
            updateEpicStatus(subtask.getEpicId());
            updateEpicTime(subtask.getEpicId());
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            removeFromPrioritizedTasks(subtask);
            historyManager.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
                updateEpicTime(epic.getId());
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}