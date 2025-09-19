package utils;

import controllers.InMemoryHistoryManager;
import controllers.InMemoryTaskManager;
import impl.HistoryManager;
import impl.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
