package org.monroe.team.toolsbox.services.impl;


import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.entities.Task;
import org.monroe.team.toolsbox.repositories.PropertyRepository;
import org.monroe.team.toolsbox.repositories.TaskRepository;
import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.model.TaskModel;
import org.monroe.team.toolsbox.us.model.impl.TaskModelImpl;
import org.monroe.team.toolsbox.us.model.impl.dependecy.JPADependency;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class TaskManagerImpl implements TaskManager{

    @Inject
    TaskRepository taskRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Override
    public TaskModel newTask(Task.Type type) {
        Task task = new Task();
        task.creationTime = System.currentTimeMillis();
        task.type = type;
        task.status = Task.ExecutionStatus.AWAITING;
        task = taskRepository.save(task);
        return createByTask(task);
    }

    private TaskModel createByTask(Task task) {
        JPADependency<Task,Integer> taskDependency = new JPADependency<Task, Integer>(taskRepository,task.id,task);
        return new TaskModelImpl(taskDependency, this, propertyRepository);
    }

    @Override
    public List<TaskModel> fetchAll() {
        return Lists.transform(taskRepository.findAll(), new Function<Task, TaskModel>() {
            @Override
            public TaskModel apply(Task task) {
                return createByTask(task);
            }
        });
    }
}
