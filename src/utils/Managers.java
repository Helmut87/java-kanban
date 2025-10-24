package utils;

import controllers.FileBackedTaskManager;
import controllers.InMemoryHistoryManager;
import controllers.InMemoryTaskManager;
import impl.HistoryManager;
import impl.TaskManager;

import java.io.File;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getFileBackedTaskManager(File file) {
        return new FileBackedTaskManager(file);
    }

    public static TaskManager loadFromFile(File file) {
        return FileBackedTaskManager.loadFromFile(file);
    }
}
