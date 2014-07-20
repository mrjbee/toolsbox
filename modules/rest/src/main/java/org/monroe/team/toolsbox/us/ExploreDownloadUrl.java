package org.monroe.team.toolsbox.us;

import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.config.LoggerConfig;
import org.monroe.team.toolsbox.services.download.UrlLazyExploreManager;
import org.monroe.team.toolsbox.services.download.UrlExplorer;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class ExploreDownloadUrl implements ExploreDownloadUrlDefinition {

    public @Resource(name = LoggerConfig.FEATURE_DOWNLOAD) Logger log;
    public @Inject UrlExplorer urlExplorer;
    public @Inject
    UrlLazyExploreManager exploreManager;
    public @Inject List<URlExplorerPlugin> explorePlugins;

    @Override
    public ExploreDownloadUrlResponse perform(String url) throws UnreachableUrlException {

        for (URlExplorerPlugin explorePlugin : explorePlugins) {
            if (explorePlugin.isLinkAcceptable(url)){
                return explorePlugin.explore(url);
            }
        }

        //If no plugin get link details directly
        DownloadUrlDetails urlDownloadDetails = null;
        urlDownloadDetails = urlExplorer.explore(url);
        return new ExploreDownloadUrlResponse(urlDownloadDetails);
    }

    public static interface URlExplorerPlugin{
        public boolean isLinkAcceptable(String url);
        public ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse explore(String url) throws ExploreDownloadUrlDefinition.UnreachableUrlException;
    }

}
