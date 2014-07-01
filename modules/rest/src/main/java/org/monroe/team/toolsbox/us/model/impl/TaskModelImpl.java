package org.monroe.team.toolsbox.us.model.impl;

import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.entities.Execution;
import org.monroe.team.toolsbox.entities.Property;
import org.monroe.team.toolsbox.entities.Task;
import org.monroe.team.toolsbox.repositories.PropertyRepository;
import org.monroe.team.toolsbox.services.ExecutionManager;
import org.monroe.team.toolsbox.services.FileManager;
import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.model.FileModel;
import org.monroe.team.toolsbox.us.model.TaskModel;
import org.monroe.team.toolsbox.us.model.impl.dependecy.Dependency;

public class TaskModelImpl implements TaskModel {

    private final Dependency<Task> taskDependency;
    private final Dependency<Execution> executionDependency;
    private final TaskManager taskManager;
    private final PropertyRepository propertyRepository;
    private final ExecutionManager executionManager;
    private final FileManager fileManager;

    public TaskModelImpl(Dependency<Task> taskDependency,
                         Dependency<Execution> executionDependency, TaskManager taskManager,
                         PropertyRepository propertyRepository,
                         ExecutionManager executionManager,
                         FileManager fileManager) {
        this.taskDependency = taskDependency;
        this.executionDependency = executionDependency;
        this.taskManager = taskManager;
        this.propertyRepository = propertyRepository;
        this.executionManager = executionManager;
        this.fileManager = fileManager;
    }

    @Override
    public org.monroe.team.toolsbox.us.model.TaskModel withProperty(String src, String value) {
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
    public ExecutionStatus getStatus() {
        check(isHealthy());
        return taskDependency.get().status;
    }

    @Override
    public String getStatusAsString() {
        return getStatus().name();
    }

    @Override
    public Type getType() {
        check(isHealthy());
        return taskDependency.get().type;
    }

    @Override
    public String getTypeAsString() {
        return getType().name();
    }

    @Override
    public Float getExecutionProgress() {
        switch (getStatus()){
            case Pending: return 0f;
            case Finished: return 1f;
            case Fails: return 0.5f;
            case Progress:
                if (!executionDependency.exists()) return 0f;
                return executionDependency.get().getProgress();
        }
        return 0f;
    }

    @Override
    public <Type> Type getProperty(String key, Class<Type> requestedType) {
        check(isHealthy());
        if (FileModel.class.equals(requestedType)){
            Integer fileId = taskDependency.get().getProperty(key,Integer.class);
            return (Type) fileManager.getById(fileId);
        }
        return taskDependency.get().getProperty(key, requestedType);
    }


    @Override
    public void execute() throws ExecutionManager.ExecutionUnavailableException {
        executeImpl(false);
    }

    private void executeImpl(boolean restart) throws ExecutionManager.ExecutionUnavailableException {
        switch (getType()){
            case COPY:
                executionManager.executeAsCopyTask(this,restart);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void restart() throws ExecutionManager.ExecutionUnavailableException {
        executeImpl(true);
    }

    @Override
    public void updateStatus(ExecutionStatus progress) {
        check(isHealthy());
        taskDependency.get().status = progress;
        taskDependency.save();
    }

    @Override
    public boolean isHardInterrupted() {
        return getStatus().equals(ExecutionStatus.Progress) && !executionDependency.exists();
    }

    private void check(boolean condition) {
        if (!condition){
            throw new IllegalStateException();
        }
    }

    private boolean isHealthy() {
        return taskDependency.exists();
    }

}
