package org.monroe.team.toolsbox.transport;
import org.monroe.team.toolsbox.services.ConfigurationManager;
import org.monroe.team.toolsbox.us.CreateCopyTaskDefinition;
import org.monroe.team.toolsbox.us.StorageLookupDefinition;
import org.monroe.team.toolsbox.us.common.BusinessExceptions;

import javax.inject.Named;
import java.util.Map;

@Named
public class Translator {

    public CreateCopyTaskDefinition.CreateCopyTaskRequest toCopyTaskCreateRequest(Map request) throws BusinessExceptions.InvalidRequestException {

        int srcFileID, dstFileID;
        boolean removeRequired;

        try{
            srcFileID = (int) Math.round((Double) request.get("srcFile"));
            dstFileID = (int) Math.round((Double) request.get("dstFile"));
            removeRequired = (Boolean) request.get("removeRequired");
        }catch (Exception e){
            throw new BusinessExceptions.InvalidRequestException();
        }
        return new CreateCopyTaskDefinition.CreateCopyTaskRequest(srcFileID, dstFileID, removeRequired);
    }

    public StorageLookupDefinition.StorageLookupRequest toStorageLookupRequest(ConfigurationManager.StorageLookupConfiguration configuration){
        return new StorageLookupDefinition.StorageLookupRequest(configuration.filePath, configuration.scanLevel);
    }

}
