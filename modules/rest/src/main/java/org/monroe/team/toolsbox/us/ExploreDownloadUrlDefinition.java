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

        public final DownloadUrlChoice[] downloadUrlChoices;

        public ExploreDownloadUrlResponse(DownloadUrlChoice[] downloadUrlChoices) {
            this.downloadUrlChoices = downloadUrlChoices;
        }

        public ExploreDownloadUrlResponse(DownloadUrlDetails downloadUrlDetails){
            this.downloadUrlChoices = new DownloadUrlChoice[]{new DownloadUrlChoice("none","none",downloadUrlDetails)};
        }
    }

    public static class DownloadUrlChoice {

        public final String name;
        public final String description;
        public final DownloadUrlDetails downloadLinkDetails;
        public final List<DownloadUrlChoice> subChoices = new ArrayList<DownloadUrlChoice>();

        public DownloadUrlChoice(String name, String description, DownloadUrlDetails downloadLinkDetails) {
            this.name = name;
            this.description = description;
            this.downloadLinkDetails = downloadLinkDetails;
        }

        public DownloadUrlChoice(DownloadUrlDetails fileDetails) {
            this.name = null;
            this.description = null;
            this.downloadLinkDetails = fileDetails;
        }

        @Override
        public String toString() {
            return "DownloadUrlChoice{\n" +
                    "\tname='" + name + '\'' + "\n"+
                    "\t, description='" + description + '\'' + "\n"+
                    "\t, downloadLinkDetails=" + downloadLinkDetails + "\n"+
                    "\t, subChoices=" + subChoices + "\n"+
                    '}';
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
