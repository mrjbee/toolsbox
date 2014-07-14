package org.monroe.team.toolsbox.us;

import org.monroe.team.toolsbox.us.common.BusinessExceptions;
import org.monroe.team.toolsbox.us.common.FileResponse;

import java.util.List;

public interface GetFileChildrenDefinition {
    public List<FileResponse> perform(String parentFileId) throws BusinessExceptions.InvalidIdException;
}
