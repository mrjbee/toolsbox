package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ServerConfigRoute  extends SpringRouteBuilder{

    private final Properties props= new Properties();
    @Override
    public void configure() throws Exception {

        from("restlet:/server/moon/sleepminutes").transform(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) props.getProperty("sleepminutes","60");
            }
        });

        from("restlet:/server/moon/sleepminutes?restletMethod=post").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                String minutes = exchange.getIn().getBody(String.class);
                props.setProperty("sleepminutes",minutes);
            }
        }).transform(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) props.getProperty("sleepminutes","60");
            }
        });

        from("restlet:/server/moon/status").transform(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) props.getProperty("status","NaN");
            }
        });

        from("restlet:/server/moon/offlineTillDate").transform(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) props.getProperty("offlineTillDate","NaN");
            }
        });

        from("restlet:/server/moon/lastDate").transform(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) props.getProperty("lastDate","NaN");
            }
        });

        from("restlet:/server/moon/status?restletMethod=post").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                String status = exchange.getIn().getBody(String.class);
                props.setProperty("status",status);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm [dd-MM-yyyy]");

                Calendar now = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
                props.setProperty("lastDate",dateFormat.format(now.getTime()));
                int minutes = Integer.parseInt(props.getProperty("sleepminutes","60"));
                if ("Offline".equals(status)){
                    now.add(Calendar.MINUTE, minutes);
                    props.setProperty("offlineTillDate",dateFormat.format(now.getTime()));
                } else {
                    props.setProperty("offlineTillDate","NaN");
                }

            }
        }).transform(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) props.getProperty("status","NaN");
            }
        });
    }
}
