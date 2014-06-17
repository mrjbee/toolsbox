package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.monroe.team.toolsbox.remote.config.us.GetSetting;
import org.monroe.team.toolsbox.remote.config.us.GetSettingDefinition;
import org.monroe.team.toolsbox.remote.config.us.SetSetting;
import org.monroe.team.toolsbox.remote.config.us.SetSettingDefinition;

import javax.inject.Named;

@Named
public class RemoteConfigSettingRoute extends RouteBuilder{

    @Override
    public void configure() throws Exception {
        from("restlet:/server/moon/{settingName}")
                .setBody(header("settingName"))
                .doTry()
                    .bean(GetSetting.class, "perform(String)")
                .doCatch(GetSettingDefinition.UnsupportedSettingException.class)
                    .setBody(constant(null))
                    .setHeader("CamelHttpResponseCode", constant(404));

        from("restlet:/server/moon/{settingName}?restletMethod=post")
                .setBody(method(RemoteConfigSettingRoute.class,"createSetSettingRequest"))
                .doTry()
                    .bean(SetSetting.class, "perform(*)")
                    .setBody(simple("body.value"))
                .doCatch(SetSettingDefinition.NotAllowedSettingException.class)
                    .setHeader("CamelHttpResponseCode",constant(400));

    }

    public static SetSettingDefinition.SetSettingRequest createSetSettingRequest(
            @Header("settingName") final String settingName,
            @Body final String value){
        return new SetSettingDefinition.SetSettingRequest() {
            @Override
            public String getName() {
                return settingName;
            }

            @Override
            public String getValue() {
                return value;
            }
        };
    }

}
