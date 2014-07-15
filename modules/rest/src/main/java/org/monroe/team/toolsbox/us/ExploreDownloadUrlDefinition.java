package org.monroe.team.toolsbox.us;

public interface ExploreDownloadUrlDefinition {

    DownloadUrlDetailsResponse perform(String url) throws UnreachableUrlException;

    public static class UnreachableUrlException extends Exception{
        public UnreachableUrlException(Throwable cause) {
            super(cause);
        }
    };

    public static class DownloadUrlDetailsResponse {

        public final String url;
        public final String fileName;
        public final String ext;
        public final String size;

        public DownloadUrlDetailsResponse(String url, String fileName, String ext, String size) {
            this.url = url;
            this.fileName = fileName;
            this.ext = ext;
            this.size = size;
        }
    }
}
