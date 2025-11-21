package handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import impl.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
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
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            // Get all tasks
            List<Task> tasks = taskManager.getAllTasks();
            String response = gson.toJson(tasks);
            sendSuccess(exchange, response);
        } else if (path.matches("/tasks/\\d+")) {
            // Get task by ID
            int id = getIntPathParameter(exchange, 2);
            Task task = taskManager.getTaskById(id);
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
        Optional<Task> taskOpt = parseJson(exchange, Task.class);
        if (taskOpt.isEmpty()) {
            sendBadRequest(exchange, "Invalid task data");
            return;
        }

        Task task = taskOpt.get();
        try {
            if (task.getId() == 0) {
                // Create new task
                Task created = taskManager.createTask(task);
                String response = gson.toJson(created);
                sendCreated(exchange, response);
            } else {
                // Update existing task
                taskManager.updateTask(task);
                sendSuccess(exchange, "{\"message\": \"Task updated successfully\"}");
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            // Delete all tasks
            taskManager.deleteAllTasks();
            sendSuccess(exchange, "{\"message\": \"All tasks deleted\"}");
        } else if (path.matches("/tasks/\\d+")) {
            // Delete task by ID
            int id = getIntPathParameter(exchange, 2);
            taskManager.deleteTaskById(id);
            sendSuccess(exchange, "{\"message\": \"Task deleted\"}");
        } else {
            sendNotFound(exchange);
        }
    }
}
