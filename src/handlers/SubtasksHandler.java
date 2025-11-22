package handlers;

import com.sun.net.httpserver.HttpExchange;
import impl.TaskManager;
import model.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager taskManager) {
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
        if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getAllSubtasks();
            String response = gson.toJson(subtasks);
            sendSuccess(exchange, response);
        } else if (path.matches("/subtasks/\\d+")) {
            String id = getPathId(path);
            if (!isValidId(id)) {
                sendBadRequest(exchange, "Invalid subtask ID");
                return;
            }

            Subtask subtask = taskManager.getSubtaskById(Integer.parseInt(id));
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
        try (InputStream inputStream = exchange.getRequestBody()) {
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            Subtask subtask = gson.fromJson(body, Subtask.class);

            if (subtask == null) {
                sendBadRequest(exchange, "Invalid subtask data");
                return;
            }

            if (subtask.getId() == 0) {
                // Create new subtask
                Subtask createdSubtask = taskManager.createSubtask(subtask);
                if (createdSubtask != null) {
                    String response = gson.toJson(createdSubtask);
                    sendCreated(exchange, response);
                } else {
                    sendHasInteractions(exchange);
                }
            } else {
                // Update existing subtask
                try {
                    taskManager.updateSubtask(subtask);
                    sendSuccess(exchange, gson.toJson(subtask));
                } catch (IllegalArgumentException e) {
                    sendHasInteractions(exchange);
                }
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Invalid JSON format");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            taskManager.deleteAllSubtasks();
            sendSuccess(exchange, "All subtasks deleted");
        } else if (path.matches("/subtasks/\\d+")) {
            String id = getPathId(path);
            if (!isValidId(id)) {
                sendBadRequest(exchange, "Invalid subtask ID");
                return;
            }

            taskManager.deleteSubtaskById(Integer.parseInt(id));
            sendSuccess(exchange, "Subtask deleted");
        } else {
            sendNotFound(exchange);
        }
    }
}