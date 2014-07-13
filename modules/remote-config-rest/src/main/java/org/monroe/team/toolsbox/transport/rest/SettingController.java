package org.monroe.team.toolsbox.transport.rest;

import org.monroe.team.toolsbox.remote.config.us.GetSettingDefinition;
import org.monroe.team.toolsbox.remote.config.us.SetSettingDefinition;
import org.monroe.team.toolsbox.remote.config.us.UpdateStatusStatistics;
import org.monroe.team.toolsbox.remote.config.us.UpdateStatusStatisticsDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class SettingController {

    @Inject GetSettingDefinition getSettingDefinition;
    @Inject SetSettingDefinition setSettingDefinition;
    @Inject UpdateStatusStatisticsDefinition updateStatusStatisticsDefinition;

    @RequestMapping("/server/{serverName}/{settingName}")
    public @ResponseBody String getSetting(@PathVariable String settingName){
        try {
            return getSettingDefinition.perform(settingName);
        } catch (GetSettingDefinition.UnsupportedSettingException e) {
            throw new Exceptions.DetailedRestException(HttpStatus.NOT_FOUND,"not_found", "Unsupported setting "+settingName, e);
        }
    }

    @RequestMapping(value = "/server/{serverName}/{settingName}",method = RequestMethod.POST)
    public @ResponseBody String setValue(@RequestBody final String body, @PathVariable final String settingName) {
        try {
            setSettingDefinition.perform(createSetRequest(body, settingName));

            if ("status".equals(settingName)){
                updateStatusStatisticsDefinition.perform(body);
            }

            return  body;
        } catch (SetSettingDefinition.NotAllowedSettingException e) {
            throw new Exceptions.DetailedRestException(HttpStatus.BAD_REQUEST,"bad_request", "Unsupported setting "+settingName, e);
        }
    }

    private SetSettingDefinition.SetSettingRequest createSetRequest(final String body, final String settingName) {
        return new SetSettingDefinition.SetSettingRequest() {
            @Override
            public String getName() {
                return settingName;
            }

            @Override
            public String getValue() {
                return body;
            }
        };
    }

}
