package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.monroe.team.toolsbox.services.ConfigurationManager;
import org.monroe.team.toolsbox.transport.common.RestRouteBuilder;
import org.monroe.team.toolsbox.us.StorageLookupDefinition;
import org.monroe.team.toolsbox.us.common.Exceptions;

import javax.inject.Inject;
import javax.inject.Named;


@Named
public class ServerConfigurationRoute extends RestRouteBuilder {

    @Inject ConfigurationManager configurationManager;
    @Inject StorageLookupDefinition storageLookup;

    @Override
    public void doConfigure() {

        from("restlet:/configuration?restletMethod=post").unmarshal().json(JsonLibrary.Gson, ConfigurationManager.Configuration.class)
            .bean(configurationManager, "setConfig");

        from("restlet:/configuration")
                .bean(configurationManager, "getConfig")
                .choice()
                    .when(body().isNull())
                    .throwException(new Exceptions.IdNotFoundException("[none]"))
                    .end()
                .marshal()
                    .json(JsonLibrary.Gson)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));

        from("timer://storageLookupLoop?fixedRate=true&period=60s")
                .routeId("StorageLookupLoop")
                .bean(configurationManager, "getStorageLookupEntryList")
                .log(LoggingLevel.DEBUG, "Storage lookup using ${body}")
                .split(body())
                    .convertBodyTo(StorageLookupDefinition.StorageLookupRequest.class)
                    .bean(storageLookup,"perform");
    }

}
