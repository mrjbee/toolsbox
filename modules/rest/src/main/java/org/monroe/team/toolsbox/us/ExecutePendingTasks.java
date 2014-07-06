package org.monroe.team.toolsbox.us;

import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.entities.Task;
import org.monroe.team.toolsbox.logging.Logs;
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

        coreLog.info("Fetching tasks. Total count:"+tasks.size());
        for(TaskModel task:tasks){
            coreLog.debug("Processing task. task:" + task);
            if (task.isHardInterrupted()){
                coreLog.info("Processing task. task:" + task);
                try {
                    log.info("[Task = {}] Restarting as hard interrupted.",task.getRef());
                    task.restart();
                } catch (ExecutionManager.ExecutionUnavailableException e) {
                    log.warn("[Task = " + task.getRef()+"] Fails! Restarting hard interrupted", e);
                }
            } else {
                coreLog.info("Processing task. task:" + task);
                if (TaskModel.ExecutionStatus.Pending.equals(task.getStatus())) {
                    try {
                        log.info("[Task = {} ] Scheduling pending task",task.getRef());
                        task.execute();
                    } catch (ExecutionManager.ExecutionUnavailableException e) {
                        log.info("[Task = " + task.getRef()+"] Tasks execution unavailable.", e);
                    } catch (Exception e){
                        log.warn("[Task = " + task.getRef() + "] Fails! Scheduling pending task.", e);
                    }
                }
            }
        }
    }
}
