package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class PingRoute extends SpringRouteBuilder {
    @Override
    public void configure() throws Exception {
        from("restlet:/ping").transform(simple("Pong [rest is up]"));
        from("restlet:/secure-ping").transform(simple("Pong [secure rest is up]"));
    }
}
