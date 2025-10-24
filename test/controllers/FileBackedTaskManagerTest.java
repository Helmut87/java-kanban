package controllers;

import enums.Status;
import exceptions.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileBackedTaskManagerTest {

    @TempDir
    Path tempDir;
    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        testFile = Files.createTempFile(tempDir, "test", ".csv").toFile();
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        // Создаем пустой менеджер и сохраняем
        FileBackedTaskManager manager = new FileBackedTaskManager(testFile);
        manager.save();

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadTasks() {
        FileBackedTaskManager manager = new FileBackedTaskManager(testFile);

        // Создаем задачи разных типов
        Task task = manager.createTask(new Task("Task 1", "Description", Status.NEW));
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask 1", "Description", Status.NEW, epic.getId()));

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем, что все задачи загрузились
        List<Task> tasks = loadedManager.getAllTasks();
        List<Epic> epics = loadedManager.getAllEpics();
        List<Subtask> subtasks = loadedManager.getAllSubtasks();

        assertEquals(1, tasks.size());
        assertEquals(1, epics.size());
        assertEquals(1, subtasks.size());

        Task loadedTask = tasks.getFirst();
        Epic loadedEpic = epics.getFirst();
        Subtask loadedSubtask = subtasks.getFirst();

        // Проверяем корректность данных
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());

        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());

        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId());
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        FileBackedTaskManager manager = new FileBackedTaskManager(testFile);

        // Создаем несколько задач
        Task task1 = manager.createTask(new Task("Task 1", "Desc 1", Status.NEW));
        Task task2 = manager.createTask(new Task("Task 2", "Desc 2", Status.IN_PROGRESS));

        Epic epic1 = manager.createEpic(new Epic("Epic 1", "Desc 1"));
        Epic epic2 = manager.createEpic(new Epic("Epic 2", "Desc 2"));

        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Desc 1", Status.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Desc 2", Status.DONE, epic1.getId()));
        Subtask subtask3 = manager.createSubtask(new Subtask("Subtask 3", "Desc 3", Status.IN_PROGRESS, epic2.getId()));

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertEquals(2, loadedManager.getAllTasks().size());
        assertEquals(2, loadedManager.getAllEpics().size());
        assertEquals(3, loadedManager.getAllSubtasks().size());

        // Проверяем связи эпиков и подзадач
        Epic loadedEpic1 = loadedManager.getEpicById(epic1.getId());
        assertNotNull(loadedEpic1);
        assertEquals(2, loadedEpic1.getSubtaskIds().size());
        assertTrue(loadedEpic1.getSubtaskIds().contains(subtask1.getId()));
        assertTrue(loadedEpic1.getSubtaskIds().contains(subtask2.getId()));
    }

    @Test
    void shouldHandleEpicStatusAfterLoading() {
        FileBackedTaskManager manager = new FileBackedTaskManager(testFile);

        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Desc", Status.NEW, epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Desc", Status.NEW, epic.getId()));

        // Меняем статусы подзадач
        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);

        // Загружаем и проверяем статус эпика
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());

        assertEquals(Status.IN_PROGRESS, loadedEpic.getStatus());
    }

    @Test
    void shouldSaveAfterEachModification() {
        FileBackedTaskManager manager = new FileBackedTaskManager(testFile);

        // Создаем задачу
        Task task = manager.createTask(new Task("Task", "Description", Status.NEW));

        // Проверяем, что сохранилось
        FileBackedTaskManager loadedManager1 = FileBackedTaskManager.loadFromFile(testFile);
        assertEquals(1, loadedManager1.getAllTasks().size());

        // Обновляем задачу
        task.setStatus(Status.DONE);
        manager.updateTask(task);

        // Проверяем, что обновление сохранилось
        FileBackedTaskManager loadedManager2 = FileBackedTaskManager.loadFromFile(testFile);
        Task loadedTask = loadedManager2.getTaskById(task.getId());
        assertEquals(Status.DONE, loadedTask.getStatus());

        // Удаляем задачу
        manager.deleteTaskById(task.getId());

        // Проверяем, что удаление сохранилось
        FileBackedTaskManager loadedManager3 = FileBackedTaskManager.loadFromFile(testFile);
        assertTrue(loadedManager3.getAllTasks().isEmpty());
    }

    @Test
    void shouldThrowManagerSaveExceptionOnSaveError() {
        File readOnlyFile = new File("/readonly/test.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(readOnlyFile);

        assertThrows(ManagerSaveException.class, () -> {
            manager.createTask(new Task("Task", "Description", Status.NEW));
        });
    }
}
