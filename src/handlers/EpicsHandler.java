package handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import impl.TaskManager;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
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
        if (path.equals("/epics")) {
            // Get all epics
            List<Epic> epics = taskManager.getAllEpics();
            String response = gson.toJson(epics);
            sendSuccess(exchange, response);
        } else if (path.matches("/epics/\\d+")) {
            // Get epic by ID
            int id = getIntPathParameter(exchange, 2);
            Epic epic = taskManager.getEpicById(id);
            if (epic != null) {
                String response = gson.toJson(epic);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            // Get subtasks for epic
            int epicId = getIntPathParameter(exchange, 2);
            List<Subtask> subtasks = taskManager.getSubtasksByEpicId(epicId);
            String response = gson.toJson(subtasks);
            sendSuccess(exchange, response);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Optional<Epic> epicOpt = parseJson(exchange, Epic.class);
        if (epicOpt.isEmpty()) {
            sendBadRequest(exchange, "Invalid epic data");
            return;
        }

        Epic epic = epicOpt.get();
        if (epic.getId() == 0) {
            // Create new epic
            Epic created = taskManager.createEpic(epic);
            String response = gson.toJson(created);
            sendCreated(exchange, response);
        } else {
            // Update existing epic
            taskManager.updateEpic(epic);
            sendSuccess(exchange, "{\"message\": \"Epic updated successfully\"}");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            // Delete all epics
            taskManager.deleteAllEpics();
            sendSuccess(exchange, "{\"message\": \"All epics deleted\"}");
        } else if (path.matches("/epics/\\d+")) {
            // Delete epic by ID
            int id = getIntPathParameter(exchange, 2);
            taskManager.deleteEpicById(id);
            sendSuccess(exchange, "{\"message\": \"Epic deleted\"}");
        } else {
            sendNotFound(exchange);
        }
    }
}