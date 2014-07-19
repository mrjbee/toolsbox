package org.monroe.team.toolsbox.config;

import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.logging.Logs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerConfig {

    public static final String FEATURE_DOWNLOAD = "Download";
    public static final String FEATURE_TASK = "Task";


    @Bean(name = FEATURE_DOWNLOAD)
    public Logger downloadLogger(){
        return Logs.forFeature(FEATURE_DOWNLOAD);
    }

    @Bean(name = FEATURE_TASK)
    public Logger taskLogger(){
        return Logs.forFeature(FEATURE_TASK);
    }

    @Bean(name = "core")
    public Logger coreLogger(){
        return Logs.core;
    }

    @Bean(name = "download")
    public Logger downloadLoggerOld(){
        return Logs.forFeature(FEATURE_DOWNLOAD);
    }

    @Bean(name = "task")
    public Logger taskLoggerOld(){
        return Logs.forFeature(FEATURE_TASK);
    }


}
