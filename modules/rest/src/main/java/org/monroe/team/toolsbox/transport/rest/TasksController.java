package org.monroe.team.toolsbox.transport.rest;

import org.monroe.team.toolsbox.transport.Translator;
import org.monroe.team.toolsbox.us.*;
import org.monroe.team.toolsbox.transport.TransportExceptions;
import org.monroe.team.toolsbox.us.common.TaskResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Controller
public class TasksController {

    @Inject
    CreateCopyTaskDefinition createCopyTask;

    @Inject
    DeleteTaskDefinition deleteTask;

    @Inject
    StopTaskDefinition killTask;

    @Inject
    GetTasksDefinition getTasks;


    @Inject
    Translator translate;
    @Inject
    CreateDownloadTaskDefinition createDownloadTask;

    @RequestMapping("/tasks")
    public @ResponseBody List<TaskResponse> getTasks(){
        return getTasks.perform();
    }


    @RequestMapping(value = "/task", method = RequestMethod.POST)
    public @ResponseBody TaskResponse createTask(@RequestBody Map taskDetails){
        if ("copy".equals(taskDetails.get("type"))){
            CreateCopyTaskDefinition.CreateCopyTaskRequest request =
                    translate.toCopyTaskCreateRequest(taskDetails);
            return createCopyTask.perform(request);
        }else if ("download".equals(taskDetails.get("type"))){
            CreateDownloadTaskDefinition.DownloadTaskCreationRequest request =
                    translate.toDownloadTaskCreateRequest(taskDetails);
            return createDownloadTask.perform(request);
        } else {
            throw new TransportExceptions.InvalidRequestException();
        }
    }

    @RequestMapping(value = "/task/{taskId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void cleanTask(@PathVariable String taskId){
        try {
            Integer id = Integer.parseInt(taskId);
            deleteTask.perform(id);
        } catch (NumberFormatException e){
            throw new TransportExceptions.InvalidIdException(taskId);
        } catch (DeleteTaskDefinition.TaskNotFoundException e) {
            throw new TransportExceptions.IdNotFoundException(taskId);
        } catch (DeleteTaskDefinition.TaskCleaningNotAllowedException e) {
            throw new TransportExceptions.InvalidOperationException("task_running","Not allowed to delete a task", e);
        }
    }

    @RequestMapping(value = "/task/{taskId}/execution", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void stopTask(@PathVariable String taskId){
        try {
            Integer id = Integer.parseInt(taskId);
            killTask.perform(id);
        } catch (NumberFormatException e) {
            throw new TransportExceptions.InvalidIdException(taskId);
        } catch (StopTaskDefinition.TaskNotFoundException e) {
            throw new TransportExceptions.IdNotFoundException(taskId);
        } catch (StopTaskDefinition.TaskStopNotAllowedException e) {
            throw new TransportExceptions.InvalidOperationException("task_not_running","Not allowed to stop a task", e);
        }
    }

}
