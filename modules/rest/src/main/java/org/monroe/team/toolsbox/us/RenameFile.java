package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.services.FileManager;
import org.monroe.team.toolsbox.services.Files;
import org.monroe.team.toolsbox.us.common.BusinessExceptions;
import org.monroe.team.toolsbox.us.common.FileResponse;
import org.monroe.team.toolsbox.us.model.FileModel;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class RenameFile implements RenameFileDefinition {

    @Inject FileManager fileManager;

    @Override
    public FileResponse perform(FileRenameRequest renameRequest) throws BusinessExceptions.FileNotFoundException, BusinessExceptions.FileOperationFailException {
        FileModel file = fileManager.getById(renameRequest.id);
        if (file == null || !file.isExistsLocally()) throw new BusinessExceptions.FileNotFoundException();
        try {
            file = file.renameTo(renameRequest.fileName);
            return new FileResponse(
                    file.getRef(),
                    file.getSimpleName(),
                    file.isDirectory(),
                    Files.convertToBestUnitsAsString(file.getByteSize()));
        }catch (Exception e){
            throw new BusinessExceptions.FileOperationFailException("Exception during removing file.",e);
        }

    }
}
