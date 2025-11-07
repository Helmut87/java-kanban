package controllers;

import enums.Status;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void shouldAddAndFindDifferentTaskTypesById() {
        Task task = taskManager.createTask(new Task("Task", "Description", Status.NEW));
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask", "Description", Status.NEW, epic.getId()));

        assertNotNull(taskManager.getTaskById(task.getId()), "Должны найти задачу по ID");
        assertNotNull(taskManager.getEpicById(epic.getId()), "Должны найти эпик по ID");
        assertNotNull(taskManager.getSubtaskById(subtask.getId()), "Должны найти подзадачу по ID");

        assertEquals(Task.class, taskManager.getTaskById(task.getId()).getClass());
        assertEquals(Epic.class, taskManager.getEpicById(epic.getId()).getClass());
        assertEquals(Subtask.class, taskManager.getSubtaskById(subtask.getId()).getClass());
    }

    @Test
    void tasksWithAssignedAndGeneratedIdsShouldNotConflict() {
        Task taskWithAssignedId = new Task("Assigned", "Description", 999, Status.NEW);

        Task task1 = taskManager.createTask(new Task("Task1", "Description", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task2", "Description", Status.NEW));

        taskManager.updateTask(taskWithAssignedId);

        assertNotNull(taskManager.getTaskById(task1.getId()));
        assertNotNull(taskManager.getTaskById(task2.getId()));

        assertNotEquals(task1.getId(), task2.getId());
        assertNotEquals(task1.getId(), 999);
        assertNotEquals(task2.getId(), 999);
    }

    @Test
    void shouldHandleMixedTaskTypesWithoutConflicts() {
        Task task = taskManager.createTask(new Task("Regular Task", "Desc", Status.NEW));
        Epic epic = taskManager.createEpic(new Epic("Epic Task", "Desc"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask", "Desc", Status.NEW, epic.getId()));

        assertEquals(1, taskManager.getAllTasks().size());
        assertEquals(1, taskManager.getAllEpics().size());
        assertEquals(1, taskManager.getAllSubtasks().size());

        assertNotNull(taskManager.getTaskById(task.getId()));
        assertNotNull(taskManager.getEpicById(epic.getId()));
        assertNotNull(taskManager.getSubtaskById(subtask.getId()));

        assertTrue(task.getId() > 0);
        assertTrue(epic.getId() > 0);
        assertTrue(subtask.getId() > 0);
    }
}