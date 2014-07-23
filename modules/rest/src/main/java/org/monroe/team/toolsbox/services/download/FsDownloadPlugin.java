package org.monroe.team.toolsbox.services.download;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.net.UrlEscapers;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.h2.mvstore.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.monroe.team.toolsbox.config.LoggerConfig;
import org.monroe.team.toolsbox.us.ExploreDownloadUrl;
import org.monroe.team.toolsbox.us.ExploreDownloadUrlDefinition;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Named
public class FsDownloadPlugin implements ExploreDownloadUrl.URlExplorerPlugin{

    //http://brb.to/video/films/i48PZgLVz0TzgLdKAMBKyM8-terminator-3-vosstanije-mashin.html
    //http://brb.to/video/serials/igbPc1a1BRRkuY2PuPLnwY-poka-stanica-spit.html
    private final static Pattern urlPattern = Pattern.compile("http:.*/brb.to/.*\\.html");

    @Resource(name = LoggerConfig.FEATURE_DOWNLOAD) Logger log;
    @Inject UrlLazyExploreManager lazyExploreManager;

    @Override
    public boolean isLinkAcceptable(String url) {
        return urlPattern.matcher(url).matches();
    }

    @Override
    public ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse explore(String url) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
        PageExplorer pageExplorer = new PageExplorer(log,lazyExploreManager);
        try{
            return pageExplorer.explore(url);
        } catch (ParsePageException e) {
            throw new ExploreDownloadUrlDefinition.UnreachableUrlException(e);
        }
    }

    public static class ParsePageException extends RuntimeException {
        public ParsePageException(String message) {
            super(message);
        }
        public ParsePageException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    public static class PageExplorer {

        //{item_id: '48O9umF7Wd7AN3PIHpVIuQ', is_perview: '', baseurl: '/video/films/i48O9umF7Wd7AN3PIHpVIuQ-terminator-3-vosstanije-mashin.html'}
        final static Pattern PATTERN_ITEM_ID = Pattern.compile(".*item_id: *'([^']*)'.*");

        private static int instanceIdCounter = 0;

        private final int instanceId;
        private final Logger log;
        private CloseableHttpClient httpclient;
        private final UrlLazyExploreManager lazyExploreManager;

        public PageExplorer(Logger log, UrlLazyExploreManager lazyExploreManager) {
            this.log = log;
            this.lazyExploreManager = lazyExploreManager;
            instanceId = ++instanceIdCounter;
        }

        //name="fl0"
        //ajax&folder=f10
        //name="fl 108624"

        public ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse explore(String url) throws ExploreDownloadUrlDefinition.UnreachableUrlException, ParsePageException {
            httpclient = HttpClientFactory.createClient();

            Document document = getPageDocument(url);
            ExploreDownloadUrlDefinition.DownloadUrlMetadata metadata = buildMetaData(document);
            String topFolderId = getTopFolderId(document);
            Document topFolderDocument = getPageDocument(url + "?ajax&folder=" + topFolderId);
            FSLazyExploreExecution execution = new FSLazyExploreExecution(this,url);
            lazyExploreManager.registerExecution("fs",execution);
            ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse answer = exploreFolder(url, topFolderDocument, execution);
            answer.metadata = metadata;
            log.info("Found choices = {}", answer);
            return answer;
        }

        private ExploreDownloadUrlDefinition.DownloadUrlMetadata buildMetaData(Document document) {
            String caption = document.select(".b-tab-item__title-inner>span").text();
            String img = document.select(".poster-main img").attr("src");
            String description = document.select("p.item-decription").text();

            return new ExploreDownloadUrlDefinition.DownloadUrlMetadata(
                    caption,
                    img,
                    description
            );
        }

        private ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse exploreFolder(String url, String folderId, FSLazyExploreExecution exploreExecution) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
            return exploreFolder(url, getPageDocument(url+"?ajax&folder="+folderId), exploreExecution);
        }

        private ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse exploreFolder(String url, Document topFolderDocument, FSLazyExploreExecution exploreExecution) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
            Elements elements = topFolderDocument.select("body>ul>li.folder");
            ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse answer = new ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse();
            for(int i=0;i<elements.size();i++){
                Element folder = elements.get(i);
                Element subFolderElement = selectElemByCss(folder, ".title:not(.link-subtype)", false);
                if (subFolderElement == null){
                    String details = selectElemByCss(folder, ".title:not(.link-simple)", true).text() +" "+ getItemDetails(folder);
                    String fileList = getFolderFileListUrl(folder);
                    String fileContents = getContent("http://brb.to"+fileList);
                    for(String fileUrl:fileContents.split("\n")){
                        String origUrl = fileUrl.trim();
                        try {
                            fileUrl = URLDecoder.decode(fileUrl.trim(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {}

                        ExploreDownloadUrlDefinition.DownloadUrlChoice choice =
                                new ExploreDownloadUrlDefinition.DownloadUrlChoice(
                                        com.google.common.io.Files.getNameWithoutExtension(fileUrl),
                                        details,
                                        origUrl);
                        answer.downloadUrlChoices.add(choice);
                    }
                } else {
                    String name = subFolderElement.children().text();
                    String details = getItemDetails(folder);
                    String subFolderId = subFolderElement.attr("name").substring(2);
                    String dataId = exploreExecution.putData(subFolderId);
                    String dataUrl = exploreExecution.generateDataUrl(dataId);
                    ExploreDownloadUrlDefinition.DownloadUrlChoice choice = new ExploreDownloadUrlDefinition.DownloadUrlChoice(name, details, dataUrl);
                    answer.downloadUrlChoices.add(choice);
                }
            }

            return answer;
        }

        private String getItemDetails(Element folder) {
            Elements detailsElement = folder.getElementsByClass("material-size");
            StringBuilder details = new StringBuilder();
            for(int j=0; j<detailsElement.size(); j++){
               details.append(detailsElement.get(j).text()+" ");
            }
            return details.toString();
        }

        private Element selectElemByCss(Element root, String cssRule, boolean strict) {
            Elements foundElements = root.select(cssRule);
            if (foundElements.size() != 1){
                if (foundElements.isEmpty() && !strict) return null;
                throw new ParsePageException("Multiple or none elements with class = "+cssRule);
            }
            return foundElements.get(0);
        }

        private Element findElemByClass(Element root, String className, boolean strict) {
            Elements foundElements = root.getElementsByClass(className);
            if (foundElements.size() != 1){
                if (foundElements.isEmpty() && !strict) return null;
                throw new ParsePageException("Multiple or none elements with class"+className);
            }
            return foundElements.get(0);
        }

        private String getFolderFileListUrl(Element folder) {
            Elements fileListElements = folder.getElementsByClass("folder-filelist");
            if (fileListElements.isEmpty()){
                return null;
            }else {
                if(fileListElements.size() != 1){
                    throw new ParsePageException("Multiple file lists");
                }
                return fileListElements.get(0).attr("href");
            }
        }

        private String getTopFolderId(Document document) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
            Elements elements = document.getElementsByClass("b-files-folders-link");
            if (elements.size() != 1){
                throw new ParsePageException("More than one (or none) root folder found = "+elements.size());
            }
            return elements.get(0).attr("name");
        }

        private Document getPageDocument(String url) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
            String content = getContent(url);
            return Jsoup.parse(content);
        }

        private String getContent(String url) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
            log.debug("Execute url:{}",url);
            HttpEntity entity = getEntity(url);
            String content = readEntity(entity);
            log.debug("Fetched content\n{}",content);
            return content;
        }

        static String parseString(Pattern pattern, String relDataString, boolean emptyAllowed) {
            String answer;
            try {
                Matcher matcher = pattern.matcher(relDataString);
                matcher.matches();
                answer =  matcher.group(1);
            }catch (Exception e){
                throw new ParsePageException("Couldn`t find in string:"+relDataString,e);
            }
            if (answer == null && !emptyAllowed) throw new ParsePageException("Couldn`t find in string:"+relDataString);
            return answer;
        }

        private Map asJson(String jsonString) {
            Map json = null;
            try {
                json = new ObjectMapper().readValue(jsonString, Map.class);
            } catch (IOException e) {
                throw new ParsePageException("Json parse exception = "+jsonString,e);
            }
            return json;
        }

        private String readEntity(HttpEntity entity) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
            InputStream is;
            try {
                is = entity.getContent();
            } catch (IOException e) {
                throw new ExploreDownloadUrlDefinition.UnreachableUrlException(e);
            }
            String content;
            try {
                content = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
            } catch (IOException e) {
                throw new ExploreDownloadUrlDefinition.UnreachableUrlException(e);
            } finally {
                if(is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        log.error("Closing stream fails",e);
                    }
                }
            }
            return content;
        }

        private HttpEntity getEntity(String url) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
            HttpGet httpget = HttpClientFactory.prepareGet(url);
            httpget.setHeader("Content-Type", "charset=UTF-8");
            HttpResponse response = null;
            try {
                response = httpclient.execute(httpget);
                if (response.getStatusLine().getStatusCode() >= 400) throw new Exception("Bad status code ="+response.getStatusLine());
            } catch (Exception e) {
                throw new ExploreDownloadUrlDefinition.UnreachableUrlException(e);
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) throw new ExploreDownloadUrlDefinition.UnreachableUrlException(new NullPointerException("No entity"));
            return entity;
        }


        public void destroy() {
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    log.warn("Exception during closing http client", e);
                }
            }
        }
    }

    public static class FSLazyExploreExecution extends UrlLazyExploreManager.LazyExecution {

        private final PageExplorer pageExplorer;
        private final String originalUrl;

        protected FSLazyExploreExecution(PageExplorer pageExplorer, String originalUrl) {
            super(60*60*1000);
            this.pageExplorer = pageExplorer;
            this.originalUrl = originalUrl;
        }

        @Override
        public ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse execute(String dataId) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
            String folderId = (String) getData(dataId);
            if (folderId == null) throw new ExploreDownloadUrlDefinition.UnreachableUrlException(new RuntimeException("Couldn`t find data = "+dataId));
            return this.pageExplorer.exploreFolder(originalUrl,folderId,this);
        }

        @Override
        public void destroy() {
            pageExplorer.destroy();
        }
    }
}