package http;

import com.google.gson.Gson;
import controllers.InMemoryTaskManager;
import enums.Status;
import impl.TaskManager;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerTasksTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    @Test
    void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Testing task", Status.NEW);
        task.setDuration(Duration.ofMinutes(30));
        task.setStartTime(LocalDateTime.now());

        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager);
        assertEquals(1, tasksFromManager.size());
        assertEquals("Test Task", tasksFromManager.get(0).getName());
    }

    @Test
    void testGetTask() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Test Task", "Description", Status.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), responseTask.getId());
        assertEquals(task.getName(), responseTask.getName());
    }

    @Test
    void testGetNonExistentTask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testUpdateTask() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Original", "Desc", Status.NEW));
        task.setName("Updated");
        task.setStatus(Status.IN_PROGRESS);

        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task updatedTask = manager.getTaskById(task.getId());
        assertEquals("Updated", updatedTask.getName());
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Test", "Desc", Status.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void testGetAllTasks() throws IOException, InterruptedException {
        manager.createTask(new Task("Task 1", "Desc 1", Status.NEW));
        manager.createTask(new Task("Task 2", "Desc 2", Status.IN_PROGRESS));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasks.length);
    }

    @Test
    void testTimeOverlap() throws IOException, InterruptedException {
        LocalDateTime baseTime = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Desc", Status.NEW);
        task1.setStartTime(baseTime);
        task1.setDuration(Duration.ofHours(2));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Desc", Status.NEW);
        task2.setStartTime(baseTime.plusHours(1)); // Overlaps
        task2.setDuration(Duration.ofHours(2));

        String taskJson = gson.toJson(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }
}
