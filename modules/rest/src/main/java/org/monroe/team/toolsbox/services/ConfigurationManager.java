package org.monroe.team.toolsbox.services;


import java.util.List;

public interface ConfigurationManager {

    public void setConfig(Configuration configuration);
    public Configuration getConfig();
    public List<StorageLookupConfiguration> getStorageLookupEntryList();


    public final class StorageLookupConfiguration {

        final public String filePath;
        final public int scanLevel;

        public StorageLookupConfiguration(String filePath, int scanLevel) {
            this.filePath = filePath;
            this.scanLevel = scanLevel;
        }
    }

    public final class Configuration{

        final public StorageLookupConfiguration[] storageLookupConfigurations;

        public Configuration(StorageLookupConfiguration... storageLookupConfigurations) {
            this.storageLookupConfigurations = storageLookupConfigurations;
        }
    }

}
