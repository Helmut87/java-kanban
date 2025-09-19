package controllers;


import enums.Status;
import impl.HistoryManager;
import model.Task;
import org.junit.jupiter.api.Test;
import utils.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {

    @Test
    void historyShouldNotExceedMaxSize() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        // Добавляем больше элементов, чем максимальный размер
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description", i, Status.NEW);
            historyManager.add(task);
        }

        assertEquals(10, historyManager.getHistory().size(), "История не должна превышать 10 элементов");

        // Проверяем, что остались последние 10 элементов
        List<Task> history = historyManager.getHistory();
        for (int i = 0; i < 10; i++) {
            assertEquals(6 + i, history.get(i).getId(), "Должны остаться последние 10 задач");
        }
    }

    @Test
    void taskDataShouldBePreservedInHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task originalTask = new Task("Original", "Original description", 1, Status.NEW);

        historyManager.add(originalTask);
        Task historyTask = historyManager.getHistory().get(0);

        assertEquals(originalTask.getName(), historyTask.getName());
        assertEquals(originalTask.getDescription(), historyTask.getDescription());
        assertEquals(originalTask.getStatus(), historyTask.getStatus());
        assertEquals(originalTask.getId(), historyTask.getId());
    }
}