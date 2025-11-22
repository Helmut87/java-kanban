package controllers;

import impl.HistoryManager;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    private final Map<Integer, Node> historyMap;
    private Node head;
    private Node tail;

    public InMemoryHistoryManager() {
        this.historyMap = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        // Удаляем существующую ноду, если задача уже есть в истории
        remove(task.getId());

        // Добавляем задачу в конец списка
        Node newNode = linkLast(task);
        historyMap.put(task.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.get(id);
        if (node != null) {
            removeNode(node);
            historyMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private Node linkLast(Task task) {
        Node newNode = new Node(task);

        if (tail == null) {
            // Список пустой
            head = newNode;
            tail = newNode;
        } else {
            // Добавляем в конец
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }

        return newNode;
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        // Обновляем ссылки соседних нод
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            // Удаляем голову
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            // Удаляем хвост
            tail = node.prev;
        }

        // Очищаем ссылки удаляемой ноды
        node.prev = null;
        node.next = null;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;

        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }

        return tasks;
    }
}
