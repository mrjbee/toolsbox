package org.monroe.team.toolsbox.us;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.entities.FileDescription;
import org.monroe.team.toolsbox.entities.Task;
import org.monroe.team.toolsbox.repositories.FileDescriptorRepository;
import org.monroe.team.toolsbox.repositories.TaskRepository;
import org.monroe.team.toolsbox.us.common.TaskResponse;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class GetTasks implements GetTasksDefinition{

    @Inject
    TaskRepository taskRepository;
    @Inject
    FileDescriptorRepository fileDescriptorRepository;

    @Override
    public List<TaskResponse> perform() {
        List<Task> taskList = taskRepository.findAll();
        return Lists.newArrayList(Iterables.transform(taskList,new Function<Task, TaskResponse>() {
            @Override
            public TaskResponse apply(Task task) {
                TaskResponse taskResponse = new TaskResponse(
                        task.id,
                        task.status.name(),
                        task.type.name(),
                        null,
                        new Float(0));

                switch (task.type){
                    case COPY:
                        Integer srcFileId = task.getProperty("src",Integer.class);
                        Integer dstFileId = task.getProperty("dst", Integer.class);
                        FileDescription srcFile = fileDescriptorRepository.findOne(srcFileId);
                        FileDescription dstFile = fileDescriptorRepository.findOne(dstFileId);
                        if (srcFile !=null && dstFile != null) {
                            taskResponse.with("src", srcFile.getSimpleName())
                                    .with("dst", dstFile.getSimpleName());
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
