package org.monroe.team.toolsbox.services.download;

import com.google.common.net.UrlEscapers;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class HttpClientFactory {

    public static CloseableHttpClient createClient(){
        return HttpClientBuilder.create()
            .setUserAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0")
            .setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                protected URI createLocationURI(String location) throws ProtocolException {
                    try {
                        return super.createLocationURI(location);
                    } catch (Exception e) {
                        location = UrlEscapers.urlFragmentEscaper().escape(location);
                        return super.createLocationURI(location);
                    }
                }
            }).build();
    }

    public static HttpGet prepareGet(String url){
        HttpGet httpget = null;
        try {
            httpget = new HttpGet(url);
        }catch (Exception e){
            url = UrlEscapers.urlFragmentEscaper().escape(url);
            httpget = new HttpGet(url);
        }
        return httpget;
    }
}
