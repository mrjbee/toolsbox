package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.monroe.team.toolsbox.services.ConfigurationManager;
import org.monroe.team.toolsbox.transport.common.RestRouteBuilder;
import org.monroe.team.toolsbox.us.CreateCopyTaskDefinition;
import org.monroe.team.toolsbox.us.ExecutePendingTasks;
import org.monroe.team.toolsbox.us.GetTasksDefinition;
import org.monroe.team.toolsbox.us.common.Exceptions;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
public class TasksRoute extends RestRouteBuilder{

    @Inject
    CreateCopyTaskDefinition createCopyTask;

    @Inject
    GetTasksDefinition getTasks;

    @Inject
    ExecutePendingTasks executePendingTasks;

    @Override
    protected void doConfigure() {
        from("timer://taskExecutionSelect?fixedRate=true&period=1s")
                .routeId("taskExecutionSelectLoop")
                .bean(executePendingTasks, "perform");

        from("restlet:/tasks")
            .routeId("getTasks")
                .bean(getTasks,"perform")
                .marshal().json(JsonLibrary.Gson);


        from("restlet:/task?restletMethod=post")
            .routeId("newTask")
               .unmarshal().json(JsonLibrary.Gson, Map.class)
               .choice()
                    .when(simple("${body[type]} == 'copy'"))
                    .convertBodyTo(CreateCopyTaskDefinition.CreateCopyTaskRequest.class)
                    .bean(createCopyTask, "perform")
               .otherwise()
                    .throwException(new Exceptions.InvalidRequestException())
                    .end()
               .marshal().json(JsonLibrary.Gson);
    }
}
