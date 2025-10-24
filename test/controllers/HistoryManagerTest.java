package controllers;

import enums.Status;
import impl.HistoryManager;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        task1 = new Task("Task 1", "Description 1", 1, Status.NEW);
        task2 = new Task("Task 2", "Description 2", 2, Status.IN_PROGRESS);
        task3 = new Task("Task 3", "Description 3", 3, Status.DONE);
    }

    @Test
    void shouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.getFirst());
    }

    @Test
    void shouldRemoveDuplicatesAndKeepLastPosition() {
        // Добавляем задачи несколько раз
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // Дубликат
        historyManager.add(task3);
        historyManager.add(task2); // Дубликат

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать 3 уникальные задачи");
        assertEquals(task1, history.get(0), "task1 должна быть на первой позиции");
        assertEquals(task3, history.get(1), "task3 должна быть на второй позиции");
        assertEquals(task2, history.get(2), "task2 должна быть на последней позиции");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertFalse(history.contains(task2));
        assertTrue(history.contains(task1));
        assertTrue(history.contains(task3));
    }

    @Test
    void shouldHandleEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldRemoveFromBeginning() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldRemoveFromEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void shouldHandleLargeHistoryWithoutDuplicates() {
        // Добавляем много задач
        for (int i = 1; i <= 100; i++) {
            Task task = new Task("Task " + i, "Description", i, Status.NEW);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(100, history.size(), "История должна содержать все 100 задач");

        // Добавляем дубликаты
        for (int i = 50; i <= 70; i++) {
            Task task = new Task("Task " + i, "Updated Description", i, Status.DONE);
            historyManager.add(task);
        }

        history = historyManager.getHistory();
        assertEquals(100, history.size(), "История всё ещё должна содержать 100 задач после обновления дубликатов");
    }
}
