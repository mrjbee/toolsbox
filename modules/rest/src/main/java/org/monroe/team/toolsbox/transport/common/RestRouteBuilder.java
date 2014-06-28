package org.monroe.team.toolsbox.transport.common;


import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.Policy;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.monroe.team.toolsbox.us.common.Exceptions;

public abstract class RestRouteBuilder extends RouteBuilder{

    public final static String POLICY_PROPAGATION_REQUIRED = "PROPAGATION_REQUIRED";
    public final static String POLICY_PROPAGATION_REQUIRED_NEW = "PROPAGATION_REQUIRES_NEW";

    @Override
    final public void configure() throws Exception {

        onException(Exceptions.IdNotFoundException.class)
                .handled(true)
                .to("log:toolsbox.Core?level=WARN&showAll=true&multiline=true&showStackTrace=true")
                .setBody(constant(null))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println(exchange);
                    }
                });

        onException(Exceptions.InvalidRequestException.class)
                .handled(true)
                .to("log:toolsbox.Core?level=WARN&showAll=true&multiline=true&showStackTrace=true")
                .setBody(constant(null))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400));

        doConfigure();
    }

    protected abstract void doConfigure();
}
