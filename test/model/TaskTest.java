package model;

import enums.Status;
import impl.TaskManager;
import org.junit.jupiter.api.Test;
import utils.Managers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", 1, Status.DONE);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
        assertEquals(task1.hashCode(), task2.hashCode(), "Хэш-коды задач с одинаковым ID должны быть равны");
    }

    @Test
    void taskShouldPreserveDataWhenAddedToManager() {
        TaskManager manager = Managers.getDefault();
        Task originalTask = new Task("Original", "Original description", Status.NEW);

        int taskId = manager.createTask(originalTask).getId();
        Task savedTask = manager.getTaskById(taskId);

        assertEquals(originalTask.getName(), savedTask.getName());
        assertEquals(originalTask.getDescription(), savedTask.getDescription());
        assertEquals(originalTask.getStatus(), savedTask.getStatus());
    }
}