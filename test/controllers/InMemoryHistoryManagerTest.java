package controllers;

import enums.Status;
import impl.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Managers;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void shouldAddAndFindDifferentTaskTypesById() {
        // Создаем задачи всех типов
        Task task = taskManager.createTask(new Task("Task", "Description", Status.NEW));
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask", "Description", Status.NEW, epic.getId()));

        // Проверяем, что можем найти по ID
        assertNotNull(taskManager.getTaskById(task.getId()), "Должны найти задачу по ID");
        assertNotNull(taskManager.getEpicById(epic.getId()), "Должны найти эпик по ID");
        assertNotNull(taskManager.getSubtaskById(subtask.getId()), "Должны найти подзадачу по ID");

        // Проверяем, что возвращаются правильные типы
        assertEquals(Task.class, taskManager.getTaskById(task.getId()).getClass());
        assertEquals(Epic.class, taskManager.getEpicById(epic.getId()).getClass());
        assertEquals(Subtask.class, taskManager.getSubtaskById(subtask.getId()).getClass());
    }

    @Test
    void tasksWithAssignedAndGeneratedIdsShouldNotConflict() {
        // Создаем задачу с назначенным ID
        Task taskWithAssignedId = new Task("Assigned", "Description", 999, Status.NEW);

        // Создаем несколько задач через менеджер (получат сгенерированные ID)
        Task task1 = taskManager.createTask(new Task("Task1", "Description", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task2", "Description", Status.NEW));

        // Добавляем задачу с назначенным ID
        taskManager.updateTask(taskWithAssignedId);

        // Проверяем, что все задачи доступны и не конфликтуют
        assertNotNull(taskManager.getTaskById(task1.getId()), "Задача со сгенерированным ID 1 должна быть доступна");
        assertNotNull(taskManager.getTaskById(task2.getId()), "Задача со сгенерированным ID 2 должна быть доступна");
        assertNotNull(taskManager.getTaskById(999), "Задача с назначенным ID 999 должна быть доступна");

        // Проверяем, что ID разные
        assertNotEquals(task1.getId(), task2.getId(), "Сгенерированные ID должны быть разными");
        assertNotEquals(task1.getId(), 999, "Сгенерированный ID не должен совпадать с назначенным");
        assertNotEquals(task2.getId(), 999, "Сгенерированный ID не должен совпадать с назначенным");
    }

    @Test
    void shouldHandleMixedTaskTypesWithoutConflicts() {
        // Создаем задачи разных типов
        Task task = taskManager.createTask(new Task("Regular Task", "Desc", Status.NEW));
        Epic epic = taskManager.createEpic(new Epic("Epic Task", "Desc"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask", "Desc", Status.NEW, epic.getId()));

        // Проверяем, что все сохранились и доступны
        assertEquals(1, taskManager.getAllTasks().size());
        assertEquals(1, taskManager.getAllEpics().size());
        assertEquals(1, taskManager.getAllSubtasks().size());

        // Проверяем, что ID не конфликтуют между разными типами
        assertNotNull(taskManager.getTaskById(task.getId()));
        assertNotNull(taskManager.getEpicById(epic.getId()));
        assertNotNull(taskManager.getSubtaskById(subtask.getId()));

        // ID могут быть любыми, но типы должны сохраняться
        assertTrue(task.getId() > 0);
        assertTrue(epic.getId() > 0);
        assertTrue(subtask.getId() > 0);
    }
}