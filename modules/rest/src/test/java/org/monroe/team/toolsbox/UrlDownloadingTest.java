package org.monroe.team.toolsbox;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class UrlDownloadingTest {

    @Ignore
    @Test public void download() throws IOException {
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet httpget = new HttpGet("http://r3---sn-pivhx-n8ve.googlevideo.com/videoplayback?expire=1405476000&fexp=902408%2C924213%2C924217%2C924222%2C930008%2C931338%2C934024%2C934030%2C938631%2C945823%2C948113%2C950826&id=o-AKd1IVClJomcqoeCQRc6YheONlI7yFXhD2vRQ9z5lnRk&signature=ED5888606D692C333601AA58A1ED8FC7E80860B5.6CCB99CD2B5F1D220B89AB6C2238B5C5B6E1C3AB&sparams=id%2Cip%2Cipbits%2Citag%2Cratebypass%2Csource%2Cupn%2Cexpire&mws=yes&ipbits=0&upn=Fp46rJEU2Fg&itag=22&mt=1405454127&ip=93.188.45.167&key=yt5&ms=au&ratebypass=yes&mv=m&sver=3&source=youtube&title=Google+I-O+2014+-+Taming+your+cloud+applications+with+intelligent+monitoring");
        //HttpGet httpget = new HttpGet("http://tempfile.ru/download/b83625cbe74c07f3daa243afebd8cbac");
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            long len = entity.getContentLength();
            InputStream inputStream = entity.getContent();
            inputStream.read();
        }
    }

}
