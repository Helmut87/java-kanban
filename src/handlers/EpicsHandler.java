package handlers;

import com.sun.net.httpserver.HttpExchange;
import impl.TaskManager;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager) {
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
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getAllEpics();
            String response = gson.toJson(epics);
            sendSuccess(exchange, response);
        } else if (path.matches("/epics/\\d+")) {
            String id = getPathId(path);
            if (!isValidId(id)) {
                sendBadRequest(exchange, "Invalid epic ID");
                return;
            }

            Epic epic = taskManager.getEpicById(Integer.parseInt(id));
            if (epic != null) {
                String response = gson.toJson(epic);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            String id = getPathId(path.split("/subtasks")[0]);
            if (!isValidId(id)) {
                sendBadRequest(exchange, "Invalid epic ID");
                return;
            }

            List<Subtask> subtasks = taskManager.getSubtasksByEpicId(Integer.parseInt(id));
            String response = gson.toJson(subtasks);
            sendSuccess(exchange, response);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            Epic epic = gson.fromJson(body, Epic.class);

            if (epic == null) {
                sendBadRequest(exchange, "Invalid epic data");
                return;
            }

            if (epic.getId() == 0) {
                Epic createdEpic = taskManager.createEpic(epic);
                String response = gson.toJson(createdEpic);
                sendCreated(exchange, response);
            } else {
                taskManager.updateEpic(epic);
                sendSuccess(exchange, gson.toJson(epic));
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Invalid JSON format");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            taskManager.deleteAllEpics();
            sendSuccess(exchange, "All epics deleted");
        } else if (path.matches("/epics/\\d+")) {
            String id = getPathId(path);
            if (!isValidId(id)) {
                sendBadRequest(exchange, "Invalid epic ID");
                return;
            }

            taskManager.deleteEpicById(Integer.parseInt(id));
            sendSuccess(exchange, "Epic deleted");
        } else {
            sendNotFound(exchange);
        }
    }
}