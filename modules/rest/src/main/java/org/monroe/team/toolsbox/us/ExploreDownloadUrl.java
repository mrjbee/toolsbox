package org.monroe.team.toolsbox.us;

import com.google.common.io.Files;
import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import javax.inject.Named;
import java.io.IOException;

@Named
public class ExploreDownloadUrl implements ExploreDownloadUrlDefinition {

    public @Resource(name = "task") Logger log;

    @Override
    public DownloadUrlDetailsResponse perform(String url) throws UnreachableUrlException {
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        try{
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = null;
            try {
                response = httpclient.execute(httpget);
            } catch (IOException e) {
                throw new UnreachableUrlException(e);
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) throw new UnreachableUrlException(new NullPointerException("No entity"));
            long byteLen = response.getEntity().getContentLength();
            if (byteLen<0)byteLen=0;
            //Content-Disposition: attachment; filename="Google I-O 2014 - Taming your cloud applications with intelligent monitoring.mp4"
            //Content-Disposition: attachment; filename="01-tenderness_nezhnostj.mp3"
            Header[] headers = response.getHeaders("Content-Disposition");
            String fileName = getFileName(headers);
            if(fileName == null){
                fileName = extractFromUrl(url);
            }
            return new DownloadUrlDetailsResponse(url,
                    Files.getNameWithoutExtension(fileName),
                    Files.getFileExtension(fileName),
                    org.monroe.team.toolsbox.services.Files.convertToBestUnitsAsString(byteLen));

        }finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.warn("Exception during closing http client", e);
            }
        }
    }

    private String extractFromUrl(String url) {
        String ext = Files.getFileExtension(url);
        if(ext.isEmpty()) ext =".unknown";
        String name = Files.getNameWithoutExtension(url);
        return name+"."+ext;
    }

    private String getFileName(Header[] headers) {
        for (Header header : headers) {
            for (HeaderElement headerElement : header.getElements()) {
                for (NameValuePair nameValuePair : headerElement.getParameters()) {
                    if (nameValuePair.getName()!=null &&"filename".equals(nameValuePair.getName().toLowerCase())){
                        return nameValuePair.getValue();
                    }
                }
            }
        }
        return null;
    }
}
