package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.common.TaskResponse;
import org.monroe.team.toolsbox.us.model.TaskModel;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CreateDownloadTask implements CreateDownloadTaskDefinition{
    @Inject
    TaskManager taskManager;

    @Override
    public TaskResponse perform(DownloadTaskCreationRequest request) {
        TaskModel taskModel = taskManager.newTask(TaskModel.Type.DOWNLOAD)
                .withProperty("fileName", request.fileName)
                .withProperty("dst", request.dstFolder.toString())
                .withProperty("url", request.url);
        //TODO: update wwith details if required
        return new TaskResponse(taskModel.getRef(), null, null, null, null, null);
    }
}
