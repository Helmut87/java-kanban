package model;


import enums.Status;
import impl.TaskManager;
import org.junit.jupiter.api.Test;
import utils.Managers;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void epicCannotAddItselfAsSubtask() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));

        // Попытка создать подзадачу с эпиком, ссылающимся на самого себя
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epic.getId());
        subtask = manager.createSubtask(subtask);

        assertNotNull(subtask, "Подзадача должна быть создана");
        assertNotEquals(subtask.getEpicId(), subtask.getId(), "Подзадача не может быть своим же эпиком");
    }

    @Test
    void epicStatusShouldBeCalculatedCorrectly() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));

        // Все подзадачи NEW -> эпик NEW
        Subtask subtask1 = manager.createSubtask(new Subtask("Sub1", "Desc", Status.NEW, epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Sub2", "Desc", Status.NEW, epic.getId()));
        assertEquals(Status.NEW, epic.getStatus());

        // Одна подзадача IN_PROGRESS -> эпик IN_PROGRESS
        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());

        // Все подзадачи DONE -> эпик DONE
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        assertEquals(Status.DONE, epic.getStatus());
    }

}