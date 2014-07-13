package org.monroe.team.toolsbox.transport.rest;

import org.monroe.team.toolsbox.remote.config.us.GetSettingDefinition;
import org.monroe.team.toolsbox.remote.config.us.SetSettingDefinition;
import org.monroe.team.toolsbox.remote.config.us.UpdateStatusStatistics;
import org.monroe.team.toolsbox.remote.config.us.UpdateStatusStatisticsDefinition;
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
            //TODO: Change exception
            return null;
        }
    }

    @RequestMapping(value = "/server/{serverName}/{settingName}",method = RequestMethod.POST)
    public @ResponseBody String setValue(@RequestBody final String body, @PathVariable final String settingName, HttpServletResponse response) throws IOException {
        try {
            setSettingDefinition.perform(createSetRequest(body, settingName));

            if ("status".equals(settingName)){
                updateStatusStatisticsDefinition.perform(body);
            }

            return  body;
        } catch (SetSettingDefinition.NotAllowedSettingException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
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
