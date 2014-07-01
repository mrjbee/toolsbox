package org.monroe.team.toolsbox.services;

import org.monroe.team.toolsbox.entities.Execution;
import org.monroe.team.toolsbox.us.model.TaskModel;
import org.monroe.team.toolsbox.us.model.impl.TaskModelImpl;

public interface ExecutionManager {

    void executeAsCopyTask(TaskModel taskModel, boolean restart) throws ExecutionUnavailableException;
    Execution getTaskExecution(Integer taskId);
    public static class ExecutionUnavailableException extends Exception {

        public final Reason reason;

        public ExecutionUnavailableException(Reason reason) {
            super(reason.humanDescription);
            this.reason = reason;
        }

        public static enum Reason {

            device_is_busy("All threads are busy"), no_file("No file available");

            public final String humanDescription;

            Reason(String humanDescription) {
                this.humanDescription = humanDescription;
            }
        }
    }
}
