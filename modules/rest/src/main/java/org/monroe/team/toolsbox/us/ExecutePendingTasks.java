package org.monroe.team.toolsbox.us;

import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.services.ExecutionManager;
import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.model.TaskModel;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class ExecutePendingTasks implements ExecutePendingTasksDefinition{

    @Inject
    TaskManager manager;

    @Resource(name = "task")
    Logger log;

    @Resource(name = "core")
    Logger coreLog;


    @Override
    public void perform() {
        List<TaskModel> tasks = manager.fetchAll();
        coreLog.debug("Fetching tasks. Total count:" + tasks.size());
        for(TaskModel task:tasks){
            coreLog.debug("Processing task. task:" + task);
            if (task.isHardInterrupted()){
                log.info("[Task = {}] Marked as hard interrupted.",task.getRef());
                task.updateStatus(TaskModel.ExecutionStatus.Restoring);
            }

            if (TaskModel.ExecutionStatus.Pending.equals(task.getStatus()) ||
                    TaskModel.ExecutionStatus.Restoring.equals(task.getStatus()) ) {
                try {
                    log.info("[Task = {} ] Scheduling pending task",task.getRef());
                    if(!TaskModel.ExecutionStatus.Pending.equals(task.getStatus()))
                        task.restart();
                    else
                        task.execute();
                } catch (ExecutionManager.ExecutionPendingException e) {
                    log.info("[Task = " + task.getRef()+"] Tasks execution unavailable. Reason: "+ e.reason);
                    task.setPendingReason(e.reason.name());
                } catch (Exception e){
                    log.warn("[Task = " + task.getRef() + "] Fails! Scheduling pending task.", e);
                    task.updateStatus(TaskModel.ExecutionStatus.Fails);
                }
            }

        }
    }
}
