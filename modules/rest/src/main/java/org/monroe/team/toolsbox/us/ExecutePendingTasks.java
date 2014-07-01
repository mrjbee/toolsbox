package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.logging.Logs;
import org.monroe.team.toolsbox.services.ExecutionManager;
import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.model.TaskModel;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ExecutePendingTasks implements ExecutePendingTasksDefinition{

    @Inject
    TaskManager manager;

    @Override
    public void perform() {
        for(TaskModel task:manager.fetchAll()){

            if (task.isHardInterrupted()){
                try {
                    task.restart();
                } catch (ExecutionManager.ExecutionUnavailableException e) {
                    Logs.core.warn("Exception during restart task = " + task.getRef(), e);
                }
            } else {
                if (TaskModel.ExecutionStatus.Pending.equals(task.getStatus())) {
                    try {
                        task.execute();
                    } catch (ExecutionManager.ExecutionUnavailableException e) {
                        Logs.core.warn("Exception during execution task = " + task.getRef(), e);
                    }
                }
            }
        }
    }
}
