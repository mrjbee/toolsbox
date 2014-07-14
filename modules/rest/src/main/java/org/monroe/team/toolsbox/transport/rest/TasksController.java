package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.model.dataformat.JsonLibrary;
import org.monroe.team.toolsbox.transport.Translator;
import org.monroe.team.toolsbox.us.CreateCopyTaskDefinition;
import org.monroe.team.toolsbox.us.ExecutePendingTasks;
import org.monroe.team.toolsbox.us.GetTasksDefinition;
import org.monroe.team.toolsbox.us.common.BusinessExceptions;
import org.monroe.team.toolsbox.us.common.TaskResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Controller
public class TasksController {

    @Inject
    CreateCopyTaskDefinition createCopyTask;

    @Inject
    GetTasksDefinition getTasks;

    @Inject
    Translator translate;

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
        }else {
            throw new BusinessExceptions.InvalidRequestException();
        }
    }
}
