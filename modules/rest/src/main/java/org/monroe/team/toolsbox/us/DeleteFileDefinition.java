package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.us.common.BusinessExceptions;

public interface DeleteFileDefinition {
    public void perform(Integer fileID) throws BusinessExceptions.FileOperationFailException,
            BusinessExceptions.FileNotFoundException;
}
