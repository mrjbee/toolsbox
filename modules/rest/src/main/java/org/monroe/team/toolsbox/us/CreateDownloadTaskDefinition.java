package org.monroe.team.toolsbox.us;


import org.monroe.team.toolsbox.us.common.TaskResponse;

public interface CreateDownloadTaskDefinition {

    public TaskResponse perform(DownloadTaskCreationRequest request);

    public static class DownloadTaskCreationRequest{

        public final Integer dstFolder;
        public final String url;
        public final String fileName;

        public DownloadTaskCreationRequest(Integer dstFolder, String url, String fileName) {
            this.dstFolder = dstFolder;
            this.url = url;
            this.fileName = fileName;
        }
    }
}
