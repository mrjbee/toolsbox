package org.monroe.team.toolsbox.services.download;

import org.monroe.team.toolsbox.us.ExploreDownloadUrl;
import org.monroe.team.toolsbox.us.ExploreDownloadUrlDefinition;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Named
public class LazyUrlExplorePlugin implements ExploreDownloadUrl.URlExplorerPlugin {

    @Inject UrlLazyExploreManager lazyExploreManager;
    private final static Pattern PLUGIN_URL = Pattern.compile("plugin:([^/]*)/([^/]*)/([^/]*)");
    //plugin:fs/id

    @Override
    public boolean isLinkAcceptable(String url) {
        return PLUGIN_URL.matcher(url).matches();
    }

    @Override
    public ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse explore(String url) throws ExploreDownloadUrlDefinition.UnreachableUrlException {
        String[] pluginDetails = parseUrl(url);
        UrlLazyExploreManager.LazyExecution execution = lazyExploreManager.getLazyExecution(pluginDetails[0], pluginDetails[1]);
        ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse response;
        synchronized (execution) {
            if (execution == null || !execution.isValid()) {
                throw new ExploreDownloadUrlDefinition.UnreachableUrlException(new RuntimeException("Execution not exists or not valid = " + url));
            }
            response = execution.execute(pluginDetails[2]);
        }
        return response;
    }

    String[] parseUrl(String url) {
       Matcher matcher = PLUGIN_URL.matcher(url);
       if (!matcher.matches()) throw new RuntimeException("Couldn`t happend");
       return new String[]{matcher.group(1),matcher.group(2),matcher.group(3)};
    }
}
