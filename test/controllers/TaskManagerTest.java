package controllers;

import enums.Status;
import impl.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    private static TaskManager staticTaskManager;

    protected abstract T createTaskManager();

    public static TaskManager getStaticTaskManager() {
        return staticTaskManager;
    }

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
        staticTaskManager = taskManager;
    }

    @Test
    void shouldAddAndFindTask() {
        Task task = new Task("Test Task", "Test Description", Status.NEW);
        Task created = taskManager.createTask(task);

        assertNotNull(created.getId());
        assertEquals(task.getName(), created.getName());
        assertEquals(task.getDescription(), created.getDescription());
        assertEquals(task.getStatus(), created.getStatus());

        Task found = taskManager.getTaskById(created.getId());
        assertEquals(created, found);
    }

    @Test
    void shouldCalculateEpicStatus() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Sub1", "Desc", Status.NEW, epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Sub2", "Desc", Status.NEW, epic.getId()));
        assertEquals(Status.NEW, epic.getStatus());

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        assertEquals(Status.DONE, epic.getStatus());

        subtask1.setStatus(Status.NEW);
        taskManager.updateSubtask(subtask1);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());

        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldHandleTaskWithTime() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofHours(2);

        Task task = new Task("Task", "Description", Status.NEW);
        task.setStartTime(startTime);
        task.setDuration(duration);

        Task created = taskManager.createTask(task);

        assertEquals(startTime, created.getStartTime());
        assertEquals(duration, created.getDuration());
        assertEquals(startTime.plus(duration), created.getEndTime());
    }

    @Test
    void shouldReturnPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 10, 0);

        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setStartTime(now.plusHours(2));
        task1.setDuration(Duration.ofHours(1));

        Task task2 = new Task("Task2", "Desc", Status.NEW);
        task2.setStartTime(now.plusHours(1));
        task2.setDuration(Duration.ofHours(1));

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals("Task2", prioritized.get(0).getName());
        assertEquals("Task1", prioritized.get(1).getName());
    }

    @Test
    void shouldNotIncludeTasksWithoutStartTimeInPrioritizedList() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofHours(1));

        Task task2 = new Task("Task2", "Desc", Status.NEW);
        // task2 без startTime

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(1, prioritized.size());
        assertEquals("Task1", prioritized.get(0).getName());
    }

    @Test
    void shouldDetectTimeOverlap() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setStartTime(baseTime);
        task1.setDuration(Duration.ofHours(2));

        Task task2 = new Task("Task2", "Desc", Status.NEW);
        task2.setStartTime(baseTime.plusHours(1));
        task2.setDuration(Duration.ofHours(2));

        assertTrue(taskManager.hasTimeOverlap(task1, task2));

        Task task3 = new Task("Task3", "Desc", Status.NEW);
        task3.setStartTime(baseTime.plusHours(3));
        task3.setDuration(Duration.ofHours(1));

        assertFalse(taskManager.hasTimeOverlap(task1, task3));
    }

    @Test
    void shouldNotDetectOverlapWithSameTask() {
        LocalDateTime baseTime = LocalDateTime.now();

        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setStartTime(baseTime);
        task1.setDuration(Duration.ofHours(2));

        assertFalse(taskManager.hasTimeOverlap(task1, task1));
    }

    @Test
    void shouldNotDetectOverlapWhenNoStartTime() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofHours(2));

        Task task2 = new Task("Task2", "Desc", Status.NEW);

        assertFalse(taskManager.hasTimeOverlap(task1, task2));
        assertFalse(taskManager.hasTimeOverlap(task2, task1));
    }

    @Test
    void shouldPreventOverlappingTasks() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setStartTime(baseTime);
        task1.setDuration(Duration.ofHours(2));
        taskManager.createTask(task1);

        Task task2 = new Task("Task2", "Desc", Status.NEW);
        task2.setStartTime(baseTime.plusHours(1));
        task2.setDuration(Duration.ofHours(2));

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createTask(task2);
        });
    }

    @Test
    void shouldAllowNonOverlappingTasks() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setStartTime(baseTime);
        task1.setDuration(Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = new Task("Task2", "Desc", Status.NEW);
        task2.setStartTime(baseTime.plusHours(2));
        task2.setDuration(Duration.ofHours(1));

        assertDoesNotThrow(() -> taskManager.createTask(task2));
    }

    @Test
    void shouldCalculateEpicTime() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask1 = taskManager.createSubtask(
                new Subtask("Sub1", "Desc", Status.NEW, epic.getId(),
                        Duration.ofHours(1), baseTime));

        Subtask subtask2 = taskManager.createSubtask(
                new Subtask("Sub2", "Desc", Status.NEW, epic.getId(),
                        Duration.ofHours(2), baseTime.plusHours(1)));

        assertEquals(baseTime, epic.getStartTime());
        assertEquals(baseTime.plusHours(3), epic.getEndTime());
    }

    @Test
    void shouldUpdateEpicTimeWhenSubtaskChanges() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask = taskManager.createSubtask(
                new Subtask("Sub", "Desc", Status.NEW, epic.getId(),
                        Duration.ofHours(1), baseTime));

        LocalDateTime newStartTime = baseTime.plusHours(2);
        subtask.setStartTime(newStartTime);
        taskManager.updateSubtask(subtask);

        assertEquals(newStartTime.plusHours(1), epic.getEndTime());
    }

    @Test
    void shouldHandleEpicWithNoSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        assertNull(epic.getStartTime());
        assertNull(epic.getEndTime());
    }

    @Test
    void shouldDeleteAllTasks() {
        Task task1 = taskManager.createTask(new Task("Task1", "Desc", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task2", "Desc", Status.NEW));

        taskManager.deleteAllTasks();

        assertTrue(taskManager.getAllTasks().isEmpty());
        assertNull(taskManager.getTaskById(task1.getId()));
        assertNull(taskManager.getTaskById(task2.getId()));
    }

    @Test
    void shouldDeleteAllEpicsAndSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Sub", "Desc", Status.NEW, epic.getId()));

        taskManager.deleteAllEpics();

        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
        assertNull(taskManager.getEpicById(epic.getId()));
        assertNull(taskManager.getSubtaskById(subtask.getId()));
    }

    @Test
    void shouldMaintainHistory() {
        Task task = taskManager.createTask(new Task("Task", "Desc", Status.NEW));
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));

        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertTrue(history.contains(task));
        assertTrue(history.contains(epic));
    }

    @Test
    void shouldRemoveFromHistoryWhenTaskDeleted() {
        Task task = taskManager.createTask(new Task("Task", "Desc", Status.NEW));
        taskManager.getTaskById(task.getId());

        taskManager.deleteTaskById(task.getId());

        List<Task> history = taskManager.getHistory();
        assertFalse(history.contains(task));
    }

    @Test
    void shouldGetSubtasksByEpicId() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Sub1", "Desc", Status.NEW, epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Sub2", "Desc", Status.NEW, epic.getId()));

        List<Subtask> epicSubtasks = taskManager.getSubtasksByEpicId(epic.getId());
        assertEquals(2, epicSubtasks.size());
        assertTrue(epicSubtasks.contains(subtask1));
        assertTrue(epicSubtasks.contains(subtask2));
    }

    @Test
    void shouldReturnEmptyListForNonExistentEpic() {
        List<Subtask> subtasks = taskManager.getSubtasksByEpicId(999);
        assertNotNull(subtasks);
        assertTrue(subtasks.isEmpty());
    }
}
