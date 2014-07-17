package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.us.common.BusinessExceptions;
import org.monroe.team.toolsbox.us.common.FileResponse;

public interface RenameFileDefinition {

    public FileResponse perform(FileRenameRequest renameRequest) throws BusinessExceptions.FileNotFoundException,
            BusinessExceptions.FileOperationFailException;

    public static class FileRenameRequest{

        public final int id;
        public final String fileName;

        public FileRenameRequest(int id, String fileName) {
            this.id = id;
            this.fileName = fileName;
        }
    }
}
