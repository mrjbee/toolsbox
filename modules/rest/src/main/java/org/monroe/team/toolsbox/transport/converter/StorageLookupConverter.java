package org.monroe.team.toolsbox.transport.converter;

import org.apache.camel.Converter;
import org.monroe.team.toolsbox.services.ConfigurationManager;
import org.monroe.team.toolsbox.us.StorageLookupDefinition;

@Converter
public class StorageLookupConverter {

    @Converter
    public StorageLookupDefinition.StorageLookupRequest toStorageLookupRequest(ConfigurationManager.StorageLookupConfiguration configuration){
           return new StorageLookupDefinition.StorageLookupRequest(configuration.filePath, configuration.scanLevel);
    }

}
