package org.monroe.team.toolsbox.services.impl;


import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.entities.Execution;
import org.monroe.team.toolsbox.entities.Task;
import org.monroe.team.toolsbox.repositories.PropertyRepository;
import org.monroe.team.toolsbox.repositories.TaskRepository;
import org.monroe.team.toolsbox.services.ExecutionManager;
import org.monroe.team.toolsbox.services.FileManager;
import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.model.TaskModel;
import org.monroe.team.toolsbox.us.model.impl.TaskModelImpl;
import org.monroe.team.toolsbox.us.model.impl.dependecy.InMemoryDependency;
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

    @Inject
    FileManager fileManager;

    @Inject
    ExecutionManager executionManager;

    @Override
    public org.monroe.team.toolsbox.us.model.TaskModel newTask(org.monroe.team.toolsbox.us.model.TaskModel.Type type) {
        Task task = new Task();
        task.creationTime = System.currentTimeMillis();
        task.type = type;
        task.status = org.monroe.team.toolsbox.us.model.TaskModel.ExecutionStatus.Pending;
        task = taskRepository.save(task);
        return createByTask(task);
    }

    private org.monroe.team.toolsbox.us.model.TaskModel createByTask(final Task task) {
        JPADependency<Task,Integer> taskDependency = new JPADependency<Task, Integer>(taskRepository,task.id,task);
        InMemoryDependency<Execution> taskExecution = createExecutionDependency(task);
        return new TaskModelImpl(taskDependency, taskExecution, this, propertyRepository, executionManager,fileManager);
    }

    private InMemoryDependency<Execution> createExecutionDependency(final Task task) {
        return new InMemoryDependency<Execution>(new InMemoryDependency.InstanceProvider<Execution>() {
            @Override
            public Execution get() {
                return executionManager.getTaskExecution(task.id);
            }
        });
    }

    @Override
    public List<org.monroe.team.toolsbox.us.model.TaskModel> fetchAll() {
        return Lists.transform(taskRepository.findAll(), new Function<Task, org.monroe.team.toolsbox.us.model.TaskModel>() {
            @Override
            public org.monroe.team.toolsbox.us.model.TaskModel apply(Task task) {
                return createByTask(task);
            }
        });
    }

    @Override
    public TaskModel taskById(Integer taskId) {
        Task task = taskRepository.findOne(taskId);
        if (task == null) return null;
        return createByTask(task);
    }
}
