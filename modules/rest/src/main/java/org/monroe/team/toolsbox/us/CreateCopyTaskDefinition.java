package org.monroe.team.toolsbox.us;


import org.monroe.team.toolsbox.us.common.TaskResponse;

public interface CreateCopyTaskDefinition {

    public TaskResponse perform(CreateCopyTaskRequest copyTaskRequest);

    public static class CreateCopyTaskRequest{

        public final Integer srcFile;
        public final Integer dstFile;
        public final Boolean removeSrcFile;

        public CreateCopyTaskRequest(Integer srfFile, Integer dstFile, Boolean removeSrcFile) {
            this.srcFile = srfFile;
            this.dstFile = dstFile;
            this.removeSrcFile = removeSrcFile;
        }
    }



}
