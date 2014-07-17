package org.monroe.team.toolsbox.us;


import org.monroe.team.toolsbox.services.FileManager;
import org.monroe.team.toolsbox.services.Files;
import org.monroe.team.toolsbox.us.common.BusinessExceptions;
import org.monroe.team.toolsbox.us.common.FileResponse;
import org.monroe.team.toolsbox.us.model.FileModel;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@Named
public class CreateFolder implements CreateFolderDefinition{

    @Inject FileManager fileManager;

    @Override
    public FileResponse perform(CreateFolderRequest request) throws BusinessExceptions.FileNotFoundException, BusinessExceptions.FileOperationFailException {
        FileModel parentFile = fileManager.getById(request.parentRef);
        if (parentFile == null || !parentFile.isExistsLocally()){
           throw new BusinessExceptions.FileNotFoundException();
        }

        FileModel file = parentFile.createFile(request.name);
        if (!file.asFile().mkdir()) throw new BusinessExceptions.FileOperationFailException("Couldn`t create dir", new IOException());
        return new FileResponse(
                file.getRef(),
                file.getSimpleName(),
                file.isDirectory(),
                Files.convertToBestUnitsAsString(file.getByteSize()));

    }
}
