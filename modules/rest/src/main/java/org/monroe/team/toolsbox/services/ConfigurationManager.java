package org.monroe.team.toolsbox.services;


import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.monroe.team.toolsbox.us.StorageLookupDefinition;

import java.util.List;

public interface ConfigurationManager {

    public void setConfig(Configuration configuration);
    public Configuration getConfig();
    public List<StorageLookupConfiguration> getStorageLookupEntryList();


    public static final class StorageLookupConfiguration {

        final public String filePath;
        final public int scanLevel;

        @JsonCreator
        public StorageLookupConfiguration(@JsonProperty("filePath")String filePath,@JsonProperty("scanLevel") int scanLevel) {
            this.filePath = filePath;
            this.scanLevel = scanLevel;
        }
    }


    public static final class Configuration{

        public StorageLookupConfiguration[] storageLookupConfigurations;

        @JsonCreator
        public Configuration(@JsonProperty(value = "storageLookupConfigurations" ) StorageLookupConfiguration... storageLookupConfigurations) {
            this.storageLookupConfigurations = storageLookupConfigurations;
        }


    }

}
