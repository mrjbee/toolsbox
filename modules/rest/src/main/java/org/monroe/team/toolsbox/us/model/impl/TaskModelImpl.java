package org.monroe.team.toolsbox.us.model.impl;

import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.entities.Property;
import org.monroe.team.toolsbox.entities.Task;
import org.monroe.team.toolsbox.repositories.PropertyRepository;
import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.model.TaskModel;
import org.monroe.team.toolsbox.us.model.impl.dependecy.Dependency;

import javax.inject.Inject;

public class TaskModelImpl implements TaskModel {
    private final Dependency<Task> taskDependency;
    private final TaskManager taskManager;
    private final PropertyRepository propertyRepository;

    public TaskModelImpl(Dependency<Task> taskDependency, TaskManager taskManager, PropertyRepository propertyRepository) {
        this.taskDependency = taskDependency;
        this.taskManager = taskManager;
        this.propertyRepository = propertyRepository;
    }

    @Override
    public TaskModel withProperty(String src, String value) {
        check(isHealthy());
        if (taskDependency.get().properties == null)
            taskDependency.get().properties = Lists.newArrayList();
        taskDependency.get().properties.add(propertyRepository.save(new Property(src, value)));
        taskDependency.save();
        return this;
    }

    @Override
    public Integer getRef() {
        check(isHealthy());
        return taskDependency.get().id;
    }

    @Override
    public Task.ExecutionStatus getStatus() {
        check(isHealthy());
        return taskDependency.get().status;
    }

    @Override
    public String getStatusAsString() {
        return getStatus().name();
    }

    @Override
    public Task.Type getType() {
        check(isHealthy());
        return taskDependency.get().type;
    }

    @Override
    public String getTypeAsString() {
        return getType().name();
    }

    @Override
    public Float getProgress() {
        return 0f;
    }

    @Override
    public <Type> Type getProperty(String src, Class<Type> requestedType) {
        check(isHealthy());
        return taskDependency.get().getProperty(src, requestedType);
    }


    private void check(boolean condition) {
        if (!condition){
            throw new IllegalStateException();
        }
    }

    public boolean isHealthy() {
        return taskDependency.exists();
    }
}
