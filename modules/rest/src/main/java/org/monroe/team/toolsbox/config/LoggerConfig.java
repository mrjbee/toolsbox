package org.monroe.team.toolsbox.config;

import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.logging.Logs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerConfig {

    @Bean(name = "task")
    public Logger taskLogger(){
        return Logs.forFeature("Task");
    }

    @Bean(name = "core")
    public Logger coreLogger(){
        return Logs.core;
    }

}
