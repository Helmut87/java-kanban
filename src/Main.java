import enums.Status;
import impl.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.Managers;

public class Main {
    public static void main(String[] args) {
            TaskManager manager = Managers.getDefault();

            System.out.println("=== Создание задач ===");

            Task task1 = manager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
            Task task2 = manager.createTask(new Task("Задача 2", "Описание задачи 2", Status.NEW));

            Epic epic1 = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
            Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic1.getId()));
            Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic1.getId()));

            Epic epic2 = manager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
            Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 3", "Описание подзадачи 3", Status.NEW, epic2.getId()));

            printAllTasks(manager);

            System.out.println("\n=== Просмотр задач (формирование истории) ===");

            // Просматриваем задачи для формирования истории
            manager.getTaskById(task1.getId());
            manager.getEpicById(epic1.getId());
            manager.getSubtaskById(subtask1.getId());

            System.out.println("История после первого просмотра:");
            manager.getHistory().forEach(System.out::println);

            // Просматриваем ещё задачи
            manager.getTaskById(task2.getId());
            manager.getSubtaskById(subtask2.getId());

            System.out.println("\nИстория после второго просмотра:");
            manager.getHistory().forEach(System.out::println);

            System.out.println("\n=== Проверка ограничения истории (10 элементов) ===");

            // Создаём и просматриваем больше задач для проверки ограничения
            for (int i = 3; i <= 12; i++) {
                Task task = manager.createTask(new Task("Задача " + i, "Описание " + i, Status.NEW));
                manager.getTaskById(task.getId());
            }

            System.out.println("История после заполнения (должна содержать 10 элементов):");
            System.out.println("Размер истории: " + manager.getHistory().size());
            manager.getHistory().forEach(System.out::println);
        }

        private static void printAllTasks(TaskManager manager) {
            System.out.println("Задачи:");
            for (Task task : manager.getAllTasks()) {
                System.out.println(task);
            }
            System.out.println("Эпики:");
            for (Epic epic : manager.getAllEpics()) {
                System.out.println(epic);
                for (Subtask subtask : manager.getSubtasksByEpicId(epic.getId())) {
                    System.out.println("--> " + subtask);
                }
            }
            System.out.println("Подзадачи:");
            for (Subtask subtask : manager.getAllSubtasks()) {
                System.out.println(subtask);
            }
            System.out.println("История:");
            for (Task task : manager.getHistory()) {
                System.out.println(task);
            }
        }
}
