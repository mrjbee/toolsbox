package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.common.TaskResponse;
import org.monroe.team.toolsbox.us.model.TaskModel;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CreateCopyTask implements CreateCopyTaskDefinition{

    @Inject
    TaskManager taskManager;

    @Override
    public TaskResponse perform(CreateCopyTaskRequest copyTaskRequest) {
        TaskModel taskModel = taskManager.newTask(TaskModel.Type.COPY)
                .withProperty("src", copyTaskRequest.srcFile.toString())
                .withProperty("dst", copyTaskRequest.dstFile.toString())
                .withProperty("remove", copyTaskRequest.removeSrcFile.toString());
       //TODO: update wwith details if required
        return new TaskResponse(taskModel.getRef(), null, null, null, null, null);
    }
}
