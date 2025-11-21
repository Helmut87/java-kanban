package handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import impl.TaskManager;
import model.Subtask;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
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
        if (path.equals("/subtasks")) {
            // Get all subtasks
            List<Subtask> subtasks = taskManager.getAllSubtasks();
            String response = gson.toJson(subtasks);
            sendSuccess(exchange, response);
        } else if (path.matches("/subtasks/\\d+")) {
            // Get subtask by ID
            int id = getIntPathParameter(exchange, 2);
            Subtask subtask = taskManager.getSubtaskById(id);
            if (subtask != null) {
                String response = gson.toJson(subtask);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Optional<Subtask> subtaskOpt = parseJson(exchange, Subtask.class);
        if (subtaskOpt.isEmpty()) {
            sendBadRequest(exchange, "Invalid subtask data");
            return;
        }

        Subtask subtask = subtaskOpt.get();
        try {
            if (subtask.getId() == 0) {
                // Create new subtask
                Subtask created = taskManager.createSubtask(subtask);
                if (created != null) {
                    String response = gson.toJson(created);
                    sendCreated(exchange, response);
                } else {
                    sendBadRequest(exchange, "Epic not found");
                }
            } else {
                // Update existing subtask
                taskManager.updateSubtask(subtask);
                sendSuccess(exchange, "{\"message\": \"Subtask updated successfully\"}");
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            // Delete all subtasks
            taskManager.deleteAllSubtasks();
            sendSuccess(exchange, "{\"message\": \"All subtasks deleted\"}");
        } else if (path.matches("/subtasks/\\d+")) {
            // Delete subtask by ID
            int id = getIntPathParameter(exchange, 2);
            taskManager.deleteSubtaskById(id);
            sendSuccess(exchange, "{\"message\": \"Subtask deleted\"}");
        } else {
            sendNotFound(exchange);
        }
    }
}