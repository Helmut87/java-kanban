package handlers;

import com.sun.net.httpserver.HttpExchange;
import impl.TaskManager;
import model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendMethodNotAllowed(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getAllTasks();
            String response = gson.toJson(tasks);
            sendSuccess(exchange, response);
        } else if (path.matches("/tasks/\\d+")) {
            String id = getPathId(path);
            if (!isValidId(id)) {
                sendBadRequest(exchange, "Invalid task ID");
                return;
            }

            Task task = taskManager.getTaskById(Integer.parseInt(id));
            if (task != null) {
                String response = gson.toJson(task);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            Task task = gson.fromJson(body, Task.class);

            if (task == null) {
                sendBadRequest(exchange, "Invalid task data");
                return;
            }

            if (task.getId() == 0) {
                // Create new task
                Task createdTask = taskManager.createTask(task);
                if (createdTask != null) {
                    String response = gson.toJson(createdTask);
                    sendCreated(exchange, response);
                } else {
                    sendHasInteractions(exchange);
                }
            } else {
                // Update existing task
                try {
                    taskManager.updateTask(task);
                    sendSuccess(exchange, gson.toJson(task));
                } catch (IllegalArgumentException e) {
                    sendHasInteractions(exchange);
                }
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Invalid JSON format");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            taskManager.deleteAllTasks();
            sendSuccess(exchange, "All tasks deleted");
        } else if (path.matches("/tasks/\\d+")) {
            String id = getPathId(path);
            if (!isValidId(id)) {
                sendBadRequest(exchange, "Invalid task ID");
                return;
            }

            taskManager.deleteTaskById(Integer.parseInt(id));
            sendSuccess(exchange, "Task deleted");
        } else {
            sendNotFound(exchange);
        }
    }
}
