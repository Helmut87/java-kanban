import enums.Status;
import impl.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        System.out.println("=== Создание задач ===");

        // Создаем две задачи
        Task task1 = manager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание задачи 2", Status.NEW));

        // Создаем эпик с тремя подзадачами
        Epic epic1 = manager.createEpic(new Epic("Эпик с подзадачами", "Описание эпика 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Описание 1", Status.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Описание 2", Status.NEW, epic1.getId()));
        Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 3", "Описание 3", Status.NEW, epic1.getId()));

        // Создаем эпик без подзадач
        Epic epic2 = manager.createEpic(new Epic("Пустой эпик", "Описание эпика 2"));

        System.out.println("Создано:");
        System.out.println(" - Задач: " + manager.getAllTasks().size());
        System.out.println(" - Эпиков: " + manager.getAllEpics().size());
        System.out.println(" - Подзадач: " + manager.getAllSubtasks().size());

        System.out.println("\n=== Запрос задач в разном порядке ===");

        // Запрашиваем задачи в разном порядке несколько раз
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getTaskById(task2.getId());
        manager.getSubtaskById(subtask2.getId());

        System.out.println("История после первого раунда запросов:");
        printHistory(manager.getHistory());

        // Повторные запросы (должны убрать дубли)
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask3.getId());

        System.out.println("\nИстория после второго раунда запросов (без дублей):");
        printHistory(manager.getHistory());

        System.out.println("\n=== Удаление задачи из истории ===");

        // Удаляем задачу, которая есть в истории
        System.out.println("Удаляем задачу 1...");
        manager.deleteTaskById(task1.getId());

        System.out.println("История после удаления задачи 1:");
        printHistory(manager.getHistory());

        System.out.println("\n=== Удаление эпика с подзадачами ===");

        // Удаляем эпик с подзадачами
        System.out.println("Удаляем эпик с подзадачами...");
        manager.deleteEpicById(epic1.getId());

        System.out.println("История после удаления эпика:");
        printHistory(manager.getHistory());

        System.out.println("\nОсталось в менеджере:");
        System.out.println(" - Задач: " + manager.getAllTasks().size());
        System.out.println(" - Эпиков: " + manager.getAllEpics().size());
        System.out.println(" - Подзадач: " + manager.getAllSubtasks().size());
    }

    private static void printHistory(List<Task> history) {
        if (history.isEmpty()) {
            System.out.println("История пуста");
            return;
        }

        for (int i = 0; i < history.size(); i++) {
            Task task = history.get(i);
            String type = getTaskType(task);
            System.out.println((i + 1) + ". " + type + ": " + task.getName() + " (ID: " + task.getId() + ")");
        }
    }

    private static String getTaskType(Task task) {
        if (task instanceof Epic) {
            return "Эпик";
        } else if (task instanceof Subtask) {
            return "Подзадача";
        } else {
            return "Задача";
        }
    }
}
