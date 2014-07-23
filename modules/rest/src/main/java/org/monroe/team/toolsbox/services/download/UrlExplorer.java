package org.monroe.team.toolsbox.services.download;

import com.google.common.net.UrlEscapers;
import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.us.ExploreDownloadUrlDefinition;

import javax.annotation.Resource;
import javax.inject.Named;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;

@Named
public class UrlExplorer {

    public @Resource(name = "download") Logger log;

    public ExploreDownloadUrlDefinition.DownloadUrlDetails explore(String url) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
        ExploreDownloadUrlDefinition.DownloadUrlDetails urlDownloadDetails;
        CloseableHttpClient httpclient = HttpClientFactory.createClient();
        try{
            urlDownloadDetails = explore(url, httpclient);
        }finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.warn("Exception during closing http client", e);
            }
        }
        return urlDownloadDetails;
    }

    public ExploreDownloadUrlDefinition.DownloadUrlDetails explore(String url, CloseableHttpClient httpclient) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
        ExploreDownloadUrlDefinition.DownloadUrlDetails urlDownloadDetails;
        HttpGet httpget = HttpClientFactory.prepareGet(url);
        httpget.setHeader("Content-Type", "charset=UTF-8");
        HttpResponse response = null;
        try {
            response = httpclient.execute(httpget);
        } catch (Exception e) {
            throw new ExploreDownloadUrlDefinition.UnreachableUrlException(e);
        }

        HttpEntity entity = response.getEntity();
        if (entity == null) throw new ExploreDownloadUrlDefinition.UnreachableUrlException(new NullPointerException("No entity"));
        if (response.getStatusLine().getStatusCode() >= 400)
            throw new ExploreDownloadUrlDefinition.UnreachableUrlException(new NullPointerException("Bad Response:"+response.getStatusLine()));
        long byteLen = response.getEntity().getContentLength();
        if (byteLen<0)byteLen=0;
        //Content-Disposition: attachment; filename="Google I-O 2014 - Taming your cloud applications with intelligent monitoring.mp4"
        //Content-Disposition: attachment; filename="01-tenderness_nezhnostj.mp3"
        Header[] headers = response.getHeaders("Content-Disposition");
        String fileName = getFileName(headers);
        if(fileName == null){
            fileName = extractFromUrl(url);
        }
        urlDownloadDetails = new ExploreDownloadUrlDefinition.DownloadUrlDetails(url,
                com.google.common.io.Files.getNameWithoutExtension(fileName),
                com.google.common.io.Files.getFileExtension(fileName),
                org.monroe.team.toolsbox.services.Files.convertToBestUnitsAsString(byteLen));
        return urlDownloadDetails;
    }

    private String extractFromUrl(String url) {
        try {
            url = URLDecoder.decode(url, "ISO8859_1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String ext = com.google.common.io.Files.getFileExtension(url);
        if(ext.isEmpty()) ext =".unknown";
        String name = com.google.common.io.Files.getNameWithoutExtension(url);
        return name+"."+ext;
    }


    private String getFileName(Header[] headers) {
        for (Header header : headers) {
            for (HeaderElement headerElement : header.getElements()) {
                for (NameValuePair nameValuePair : headerElement.getParameters()) {
                    if (nameValuePair.getName()!=null &&"filename".equals(nameValuePair.getName().toLowerCase())){
                        try {
                            String fileName = nameValuePair.getValue();
                            fileName = URLEncoder.encode(fileName, "ISO8859_1");
                            fileName = URLDecoder.decode(fileName, "UTF-8");
                            return fileName;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return null;
    }

}
