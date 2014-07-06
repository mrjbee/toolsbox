package org.monroe.team.toolsbox.us.model;

import org.monroe.team.toolsbox.services.ExecutionManager;

public interface TaskModel {

    TaskModel withProperty(String src, String value);

    Integer getRef();

    ExecutionStatus getStatus();

    String getStatusAsString();

    Type getType();

    String getTypeAsString();

    Float getExecutionProgress();

    <Type> Type getProperty(String src, Class<Type> requestedType);

    void execute() throws ExecutionManager.ExecutionUnavailableException;

    void restart() throws ExecutionManager.ExecutionUnavailableException;

    void updateStatus(ExecutionStatus progress);

    boolean isHardInterrupted();

    String getEstimationDateString();

    public static enum Type{
        COPY, TRANSFER, DELETE
    }

    public static enum ExecutionStatus {
        Pending, Progress, Finished, Fails
    }

}
