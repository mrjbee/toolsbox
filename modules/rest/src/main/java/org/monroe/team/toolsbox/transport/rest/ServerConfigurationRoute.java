package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.Exchange;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.monroe.team.toolsbox.services.ConfigurationManager;
import org.monroe.team.toolsbox.us.StorageLookupDefinition;
import org.monroe.team.toolsbox.us.common.BusinessExceptions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;


@Controller
public class ServerConfigurationRoute {

    @Inject ConfigurationManager configurationManager;

    @RequestMapping(value = "/configuration",method = RequestMethod.POST)
    public void configure(@RequestBody ConfigurationManager.Configuration config){
        configurationManager.setConfig(config);
    }

    @RequestMapping(value = "/configuration")
    public @ResponseBody ConfigurationManager.Configuration getConfig() {
       return configurationManager.getConfig();
    }

}
