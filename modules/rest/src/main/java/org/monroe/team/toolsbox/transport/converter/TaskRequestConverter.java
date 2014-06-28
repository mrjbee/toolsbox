package org.monroe.team.toolsbox.transport.converter;

import org.apache.camel.Converter;
import org.monroe.team.toolsbox.us.CreateCopyTaskDefinition;
import org.monroe.team.toolsbox.us.common.Exceptions;

import java.util.Map;

@Converter
public class TaskRequestConverter {

    @Converter
    public CreateCopyTaskDefinition.CreateCopyTaskRequest toCopyTaskCreateRequest(Map request) throws Exceptions.InvalidRequestException {

        int srcFileID, dstFileID;
        boolean removeRequired;

        try{
           srcFileID = (int) Math.round((Double) request.get("srcFile"));
           dstFileID = (int) Math.round((Double) request.get("dstFile"));
           removeRequired = (Boolean) request.get("removeRequired");
        }catch (Exception e){
            throw new Exceptions.InvalidRequestException();
        }
        return new CreateCopyTaskDefinition.CreateCopyTaskRequest(srcFileID, dstFileID, removeRequired);
    };
}