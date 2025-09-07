import controllers.InMemoryTaskManager;
import enums.Status;
import impl.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = new InMemoryTaskManager();

        System.out.println("=== Создание задач ===");

        // Создаем две обычные задачи
        Task task1 = manager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание задачи 2", Status.NEW));

        // Создаем эпик с двумя подзадачами
        Epic epic1 = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic1.getId()));

        // Создаем эпик с одной подзадачей
        Epic epic2 = manager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 3", "Описание подзадачи 3", Status.NEW, epic2.getId()));

        System.out.println("\n=== Все задачи ===");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        System.out.println("\n=== Изменение статусов ===");

        // Меняем статусы
        task1.setStatus(Status.DONE);
        manager.updateTask(task1);

        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);

        subtask3.setStatus(Status.DONE);
        manager.updateSubtask(subtask3);

        System.out.println("После изменения статусов:");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        System.out.println("\n=== Подзадачи эпика 1 ===");
        System.out.println(manager.getSubtasksByEpicId(epic1.getId()));

        System.out.println("\n=== Удаление задач ===");

        // Удаляем одну задачу и один эпик
        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic2.getId());

        System.out.println("После удаления:");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

    }
}
