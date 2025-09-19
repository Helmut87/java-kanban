package utils;


import enums.Status;
import impl.HistoryManager;
import impl.TaskManager;
import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {
    @Test
    void getDefaultShouldReturnInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер не должен быть null");

        // Проверяем, что менеджер готов к работе
        Task task = manager.createTask(new Task("Test", "Test", Status.NEW));
        assertNotNull(task.getId(), "Задача должна получить ID");

        Task retrieved = manager.getTaskById(task.getId());
        assertNotNull(retrieved, "Менеджер должен возвращать сохранённые задачи");
    }

    @Test
    void getDefaultHistoryShouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории не должен быть null");

        // Проверяем, что менеджер истории готов к работе
        historyManager.add(new Task("Test", "Test", 1, Status.NEW));
        assertEquals(1, historyManager.getHistory().size(), "Менеджер истории должен сохранять задачи");
    }
}