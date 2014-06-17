package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.monroe.team.toolsbox.remote.config.us.*;

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
                .doCatch(SetSettingDefinition.NotAllowedSettingException.class)
                    .setHeader("CamelHttpResponseCode",constant(400))
                    .stop()
                .end()
                .setBody(simple("body.value"))
                .choice()
                    .when().simple("${in.header.settingName} == 'status'")
                        .bean(UpdateStatusStatistics.class, "perform(*)")
                    .end()
                .end();
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
