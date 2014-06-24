package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.monroe.team.toolsbox.services.ConfigurationManager;
import org.monroe.team.toolsbox.transport.common.RestRouteBuilder;
import org.monroe.team.toolsbox.us.common.Exceptions;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
public class TasksRoute extends RestRouteBuilder{

    @Override
    protected void doConfigure() {
       from("restlet:/task?restletMethod=post")
            .routeId("newTask")
               .unmarshal().json(JsonLibrary.Gson, Map.class)
               .choice()
                    .when(simple("${body[type]} == 'copy'"))
                    .process(new Processor() {
                       @Override
                       public void process(Exchange exchange) throws Exception {
                           System.out.println(exchange.getIn().getBody());
                       }
                    })
                    .otherwise()
                        .throwException(new Exceptions.InvalidRequestException());
    }
}
