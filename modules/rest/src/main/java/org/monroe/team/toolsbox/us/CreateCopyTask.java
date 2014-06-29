package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.entities.Property;
import org.monroe.team.toolsbox.entities.Task;
import org.monroe.team.toolsbox.repositories.PropertyRepository;
import org.monroe.team.toolsbox.repositories.TaskRepository;
import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.common.TaskResponse;
import org.monroe.team.toolsbox.us.model.TaskModel;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;

@Named
public class CreateCopyTask implements CreateCopyTaskDefinition{

    @Inject
    TaskRepository taskRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    TaskManager taskManager;

    @Override
    public TaskResponse perform(CreateCopyTaskRequest copyTaskRequest) {
        TaskModel taskModel = taskManager.newTask(Task.Type.COPY)
                .withProperty("src", copyTaskRequest.srcFile.toString())
                .withProperty("dst", copyTaskRequest.dstFile.toString())
                .withProperty("remove", copyTaskRequest.removeSrcFile.toString());
       //TODO: update wwith details if required
        return new TaskResponse(taskModel.getRef(), null, null, null, null);
    }
}
