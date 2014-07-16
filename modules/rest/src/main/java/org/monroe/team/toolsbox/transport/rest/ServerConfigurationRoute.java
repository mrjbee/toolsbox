package org.monroe.team.toolsbox.transport.rest;

import org.monroe.team.toolsbox.services.ConfigurationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;


@Controller
public class ServerConfigurationRoute {

    @Inject ConfigurationManager configurationManager;

    @RequestMapping(value = "/configuration",method = RequestMethod.POST)
    public @ResponseBody ConfigurationManager.Configuration configure(@RequestBody ConfigurationManager.Configuration config){
        configurationManager.setConfig(config);
        return getConfig();
    }

    @RequestMapping(value = "/configuration")
    public @ResponseBody ConfigurationManager.Configuration getConfig() {
       return configurationManager.getConfig();
    }

}
