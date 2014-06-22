package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.monroe.team.toolsbox.entities.Storage;
import org.monroe.team.toolsbox.repositories.StorageRepository;
import org.monroe.team.toolsbox.services.ConfigurationManager;
import org.monroe.team.toolsbox.us.StorageLookupDefinition;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;


@Named
public class ServerConfigurationRoute extends RouteBuilder {

    @Inject ConfigurationManager configurationManager;
    @Inject StorageLookupDefinition storageLookup;
    @Inject StorageRepository storageRepository;

    @Override
    public void configure() throws Exception {
        from("restlet:/test/{testName}").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                List<Storage> storageList = storageRepository.findAll();
                System.out.println(storageList);
                String testPath = exchange.getIn().getHeader("testName", String.class);
                Storage storage = new Storage(testPath,testPath, Storage.StorageType.PORTABLE);
                storageRepository.save(storage);
            }
        });

        from("restlet:/configuration?restletMethod=post").unmarshal().json(JsonLibrary.Gson, ConfigurationManager.Configuration.class)
            .bean(configurationManager, "setConfig");

        from("timer://storageLookupLoop?fixedRate=true&period=60s")
                .routeId("StorageLookupLoop")
                .bean(configurationManager, "getStorageLookupEntryList")
                .log(LoggingLevel.DEBUG, "Storage lookup using ${body}")
                .split(body())
                    .convertBodyTo(StorageLookupDefinition.StorageLookupRequest.class)
                    .bean(storageLookup,"perform");
    }

}
