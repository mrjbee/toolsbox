package org.monroe.team.toolsbox.transport.rest;


import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.monroe.team.toolsbox.transport.common.RestRouteBuilder;
import org.monroe.team.toolsbox.us.GetFileChildrenDefinition;
import org.monroe.team.toolsbox.us.GetStoragesDefinition;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class FilesRoute extends RestRouteBuilder{

    @Inject GetFileChildrenDefinition getFileChildren;
    @Inject GetStoragesDefinition getStorages;

    @Override
    public void doConfigure() {
        from("restlet:/file/{fileId}/children")
                .routeId("fileExplorer")
                    .setBody(header("fileId"))
                    .bean(getFileChildren,"perform")
                    .marshal().json(JsonLibrary.Gson)
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));

        from("restlet:/storages")
                .routeId("storagesExplorer")
                .bean(getStorages,"perform")
                .marshal().json(JsonLibrary.Gson)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));
    }
}
