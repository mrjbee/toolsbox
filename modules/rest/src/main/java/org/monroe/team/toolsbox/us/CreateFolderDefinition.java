package org.monroe.team.toolsbox.us;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.monroe.team.toolsbox.us.common.BusinessExceptions;
import org.monroe.team.toolsbox.us.common.FileResponse;

public interface CreateFolderDefinition {

    public FileResponse perform(CreateFolderRequest request) throws BusinessExceptions.FileNotFoundException, BusinessExceptions.FileOperationFailException;

    public static class CreateFolderRequest {
        public final int parentRef;
        public final String name;

        @JsonCreator
        public CreateFolderRequest(@JsonProperty("parentRef") int parentRef, @JsonProperty("name") String name) {
            this.parentRef = parentRef;
            this.name = name;
        }
    }

}
