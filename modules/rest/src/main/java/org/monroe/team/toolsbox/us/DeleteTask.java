package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.model.TaskModel;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class DeleteTask implements DeleteTaskDefinition {

    @Inject
    TaskManager taskManager;

    @Override
    public void perform(Integer taskId) throws TaskNotFoundException, TaskCleaningNotAllowedException {
        TaskModel task = taskManager.taskById(taskId);
        if (task == null){
            throw new TaskNotFoundException();
        }
        boolean result = task.destroy();
        if (!result) throw new TaskCleaningNotAllowedException();
    }
}
