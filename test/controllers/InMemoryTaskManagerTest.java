package controllers;

import enums.Status;
import impl.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void shouldRemoveSubtasksWhenEpicIsDeleted() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Subtask 1", "Desc", Status.NEW, epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Subtask 2", "Desc", Status.NEW, epic.getId()));

        // Просматриваем задачи для добавления в историю
        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getSubtaskById(subtask2.getId());

        // Удаляем эпик
        taskManager.deleteEpicById(epic.getId());

        // Проверяем, что подзадачи удалены
        assertNull(taskManager.getSubtaskById(subtask1.getId()));
        assertNull(taskManager.getSubtaskById(subtask2.getId()));
        assertNull(taskManager.getEpicById(epic.getId()));

        // Проверяем, что задачи удалены из истории
        List<Task> history = taskManager.getHistory();
        assertFalse(history.contains(epic));
        assertFalse(history.contains(subtask1));
        assertFalse(history.contains(subtask2));
    }

    @Test
    void shouldNotContainOldSubtaskIdsAfterDeletion() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Subtask 1", "Desc", Status.NEW, epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Subtask 2", "Desc", Status.NEW, epic.getId()));

        List<Integer> subtaskIds = epic.getSubtaskIds();
        assertEquals(2, subtaskIds.size());

        // Удаляем одну подзадачу
        taskManager.deleteSubtaskById(subtask1.getId());

        // Проверяем, что эпик обновил список подзадач
        subtaskIds = epic.getSubtaskIds();
        assertEquals(1, subtaskIds.size());
        assertFalse(subtaskIds.contains(subtask1.getId()));
        assertTrue(subtaskIds.contains(subtask2.getId()));
    }

    @Test
    void shouldMaintainDataIntegrityAfterTaskModification() {
        Task originalTask = taskManager.createTask(new Task("Original", "Original desc", Status.NEW));
        int taskId = originalTask.getId();

        // Модифицируем задачу через сеттеры
        originalTask.setName("Modified");
        originalTask.setDescription("Modified desc");
        originalTask.setStatus(Status.DONE);

        // Проверяем, что менеджер сохранил изменения
        Task savedTask = taskManager.getTaskById(taskId);
        assertEquals("Modified", savedTask.getName());
        assertEquals("Modified desc", savedTask.getDescription());
        assertEquals(Status.DONE, savedTask.getStatus());
    }

    @Test
    void shouldNotHaveDuplicateTasksInHistory() {
        Task task = taskManager.createTask(new Task("Task", "Description", Status.NEW));
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        // Многократно просматриваем одни и те же задачи
        for (int i = 0; i < 5; i++) {
            taskManager.getTaskById(task.getId());
            taskManager.getEpicById(epic.getId());
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать только 2 уникальные задачи");
        assertTrue(history.contains(task));
        assertTrue(history.contains(epic));
    }

    @Test
    void shouldPreserveHistoryOrder() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Desc", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Desc", Status.NEW));
        Task task3 = taskManager.createTask(new Task("Task 3", "Desc", Status.NEW));

        // Просматриваем в определённом порядке
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));

        // Просматриваем снова - порядок должен измениться
        taskManager.getTaskById(task1.getId());

        history = taskManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task1, history.get(2));
    }
}