package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.model.TaskModel;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class StopTask implements StopTaskDefinition {

    @Inject
    TaskManager taskManager;

    @Override
    public void perform(Integer taskId) throws TaskNotFoundException, TaskStopNotAllowedException {
        TaskModel task = taskManager.taskById(taskId);
        if (task == null){
            throw new TaskNotFoundException();
        }
        boolean result = task.stop();
        if (!result) throw new TaskStopNotAllowedException();
    }
}
