package org.monroe.team.toolsbox.us;

import java.util.ArrayList;
import java.util.List;

public interface ExploreDownloadUrlDefinition {

    ExploreDownloadUrlResponse perform(String url) throws UnreachableUrlException;

    public static class UnreachableUrlException extends Exception{
        public UnreachableUrlException(Throwable cause) {
            super(cause);
        }
    }

    public class ExploreDownloadUrlResponse{

        public final List<DownloadUrlChoice> downloadUrlChoices = new ArrayList<DownloadUrlChoice>();
        public final DownloadUrlDetails downloadUrlDetails;

        public ExploreDownloadUrlResponse() {
            this.downloadUrlDetails = null;
        }

        public ExploreDownloadUrlResponse(DownloadUrlDetails downloadUrlDetails) {
            this.downloadUrlDetails = downloadUrlDetails;
        }
    }

    public static class DownloadUrlChoice {

        public final String name;
        public final String description;
        public final String ref;

        public DownloadUrlChoice(String name, String description, String ref) {
            this.name = name;
            this.description = description;
            this.ref = ref;
        }
    }

    public static class DownloadUrlDetails {

        public final String url;
        public final String fileName;
        public final String ext;
        public final String size;

        public DownloadUrlDetails(String url, String fileName, String ext, String size) {
            this.url = url;
            this.fileName = fileName;
            this.ext = ext;
            this.size = size;
        }

        @Override
        public String toString() {
            return "DownloadUrlDetails{" +
                    "url='" + url + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", ext='" + ext + '\'' +
                    ", size='" + size + '\'' +
                    '}';
        }
    }
}
