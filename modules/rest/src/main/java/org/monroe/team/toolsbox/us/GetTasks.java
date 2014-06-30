package org.monroe.team.toolsbox.us;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.services.FileManager;
import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.common.TaskResponse;
import org.monroe.team.toolsbox.us.model.FileModel;
import org.monroe.team.toolsbox.us.model.TaskModel;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class GetTasks implements GetTasksDefinition{


    @Inject
    TaskManager taskManager;

    @Inject
    FileManager fileManager;

    @Override
    public List<TaskResponse> perform() {
        List<TaskModel> taskList = taskManager.fetchAll();
        return Lists.newArrayList(Iterables.transform(taskList, new Function<TaskModel, TaskResponse>() {
            @Override
            public TaskResponse apply(TaskModel task) {
                TaskResponse taskResponse = new TaskResponse(
                        task.getRef(),
                        task.getStatusAsString(),
                        task.getTypeAsString(),
                        null,
                        task.getExecutionProgress());

                switch (task.getType()){
                    case COPY:
                        Integer srcFileId = task.getProperty("src",Integer.class);
                        Integer dstFileId = task.getProperty("dst", Integer.class);
                        FileModel srcFile = fileManager.getById(srcFileId);
                        FileModel dstFile = fileManager.getById(dstFileId);

                        if (srcFile !=null && dstFile != null) {
                            taskResponse.with("src", srcFile.getSimpleName())
                                    .with("dst", dstFile.getStorage().getLabel()+"/../"+dstFile.getSimpleName());
                        } else {
                            taskResponse.with("src", "NaN")
                                    .with("dst","NaN");

                        }

                        break;
                    default:
                        throw new RuntimeException("Unsupported type");
                }
                return taskResponse;
            }
        }));
    }
}
