package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.entities.Property;
import org.monroe.team.toolsbox.entities.Task;
import org.monroe.team.toolsbox.repositories.PropertyRepository;
import org.monroe.team.toolsbox.repositories.TaskRepository;
import org.monroe.team.toolsbox.us.common.TaskResponse;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;

@Named
public class CreateCopyTask implements CreateCopyTaskDefinition{

    @Inject
    TaskRepository taskRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Override
    public TaskResponse perform(CreateCopyTaskRequest copyTaskRequest) {

        Task task = new Task();
        task.creationTime = System.currentTimeMillis();
        task.type = Task.Type.COPY;
        task.status = Task.ExecutionStatus.AWAITING;

        task = taskRepository.save(task);

        task.properties = new ArrayList<Property>(3);
        task.properties.add(propertyRepository.save(new Property("src", copyTaskRequest.srcFile.toString())));
        task.properties.add(propertyRepository.save(new Property("dst", copyTaskRequest.dstFile.toString())));
        task.properties.add(propertyRepository.save(new Property("remove", copyTaskRequest.removeSrcFile.toString())));

        task = taskRepository.save(task);

        //TODO: update wwith details if required
        return new TaskResponse(task.id, null, null, null, null);
    }
}
