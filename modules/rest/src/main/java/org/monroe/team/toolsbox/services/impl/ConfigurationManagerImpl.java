package org.monroe.team.toolsbox.services.impl;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.logging.Logs;
import org.monroe.team.toolsbox.services.ConfigurationManager;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Named
final public class ConfigurationManagerImpl implements ConfigurationManager {

    final static String configurationFilPath = "remfly-conf.json";
    final static Logger log = Logs.core;

    Configuration configuration;

    @Override
    public synchronized void setConfig(Configuration config) {
        configuration = config;
        updateLocalFile();
    }

    @Override
    public synchronized Configuration getConfig() {
        return configuration;
    }

    @Override
    public List<StorageLookupConfiguration> getStorageLookupEntryList() {
        if (configuration ==null ||
                configuration.storageLookupConfigurations == null) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(configuration.storageLookupConfigurations);
    }

    private void updateLocalFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(configuration);
        try {
            Files.write(jsonOutput, new File(configurationFilPath), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public synchronized void loadLocalFile(){
        File configFile = new File(configurationFilPath);
        if (!configFile.exists()){
            log.info("Configuration file not found (path={})",configFile.getAbsolutePath());
            return;
        }
        try {
           List<String> contentLines = Files.readLines(configFile, Charsets.UTF_8);
           String confJson = Joiner.on("\n").join(contentLines);
           log.info("Configuration loaded from file (path={}):\n{}",configFile.getAbsolutePath(), confJson);
           Gson gson = new GsonBuilder().create();
           Configuration conf = gson.fromJson(confJson,Configuration.class);
           configuration = conf;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
