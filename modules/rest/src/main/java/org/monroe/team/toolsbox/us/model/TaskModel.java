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

    boolean execute() throws ExecutionManager.ExecutionPendingException;

    boolean restart() throws ExecutionManager.ExecutionPendingException;

    void updateStatus(ExecutionStatus progress);

    boolean isHardInterrupted();

    String getEstimationDateString();

    boolean destroy();

    boolean stop();

    String getExecutionSpeed();

    void setPendingReason(String reason);

    String getPendingReason();

    public static enum Type{
        COPY, TRANSFER, DOWNLOAD, DELETE
    }

    public static enum ExecutionStatus {

        Pending, Progress, Finished, Fails, Killed, Restoring;

        public static boolean isExecutionAwaiting(ExecutionStatus status) {
            return Pending.equals(status) || Restoring.equals(status);
        }
    }

}
