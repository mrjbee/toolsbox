package org.monroe.team.toolsbox.services;

import org.monroe.team.toolsbox.entities.Task;
import org.monroe.team.toolsbox.us.model.TaskModel;

import java.util.List;

public interface TaskManager {

    TaskModel newTask(TaskModel.Type type);
    List<TaskModel> fetchAll();

    TaskModel taskById(Integer taskId);
}
