package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.services.FileManager;
import org.monroe.team.toolsbox.us.common.BusinessExceptions;
import org.monroe.team.toolsbox.us.model.FileModel;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class DeleteFile implements DeleteFileDefinition{

    @Inject FileManager fileManager;

    @Override
    public void perform(Integer fileID) throws BusinessExceptions.FileOperationFailException, BusinessExceptions.FileNotFoundException {
        FileModel fileModel = fileManager.getById(fileID);
        if (fileModel == null || !fileModel.isExistsLocally()) throw new BusinessExceptions.FileNotFoundException();
        try {
            fileModel.delete();
        }catch (Exception e){
            throw new BusinessExceptions.FileOperationFailException("Exception during removing file.",e);
        }
    }
}
