package org.monroe.team.toolsbox.services;

import org.monroe.team.toolsbox.entities.Execution;
import org.monroe.team.toolsbox.us.model.TaskModel;
import org.monroe.team.toolsbox.us.model.impl.TaskModelImpl;

public interface ExecutionManager {

    void executeAsCopyTask(TaskModel taskModel, boolean restart) throws ExecutionPendingException;
    void executeAsDownloadTask(TaskModelImpl taskModel, boolean restart) throws ExecutionPendingException;

    Execution getTaskExecution(Integer taskId);

    public static class ExecutionPendingException extends Exception {

        public final Reason reason;

        public ExecutionPendingException(Reason reason) {
            super(reason.humanDescription);
            this.reason = reason;
        }

        public static enum Reason {

            device_is_busy("All device are busy"),
            no_file("No file available"),
            max_download("Max downloads at once");

            public final String humanDescription;

            Reason(String humanDescription) {
                this.humanDescription = humanDescription;
            }

            @Override
            public String toString() {
                return name()+" ["+humanDescription+"]";
            }
        }
    }
}
