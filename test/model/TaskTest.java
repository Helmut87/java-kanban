package model;

import enums.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", 1, Status.DONE);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
        assertEquals(task1.hashCode(), task2.hashCode(), "Хэш-коды задач с одинаковым ID должны быть равны");
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 1, Status.NEW, 10);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 1, Status.DONE, 20);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны");
        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "Хэш-коды подзадач с одинаковым ID должны быть равны");
    }

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Epic 1", "Description 1", 1, Status.NEW);
        Epic epic2 = new Epic("Epic 2", "Description 2", 1, Status.DONE);

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны");
        assertEquals(epic1.hashCode(), epic2.hashCode(), "Хэш-коды эпиков с одинаковым ID должны быть равны");
    }

    @Test
    void tasksWithDifferentIdsShouldNotBeEqual() {
        Task task1 = new Task("Task", "Description", 1, Status.NEW);
        Task task2 = new Task("Task", "Description", 2, Status.NEW);

        assertNotEquals(task1, task2, "Задачи с разными ID не должны быть равны");
    }
}