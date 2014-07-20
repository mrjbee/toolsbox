package org.monroe.team.toolsbox.services.download;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.monroe.team.toolsbox.logging.Logs;
import org.monroe.team.toolsbox.us.ExploreDownloadUrlDefinition;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

@Ignore
public class FsDownloadPluginTest {

    private final FsDownloadPlugin testInstance = new FsDownloadPlugin();

    @Before
    public void init(){
        testInstance.log = Logs.core;
    }

    @Test
    public void shouldExtract(){
        String src = "{item_id: '48O9umF7Wd7AN3PIHpVIuQ', is_perview: '', baseurl: '/video/films/i48O9umF7Wd7AN3PIHpVIuQ-terminator-3-vosstanije-mashin.html'";
        Pattern itemIdPattern = FsDownloadPlugin.PageExplorer.PATTERN_ITEM_ID;
        assertEquals(
                FsDownloadPlugin.PageExplorer.parseString(itemIdPattern,src,false),
                "48O9umF7Wd7AN3PIHpVIuQ");
    }

    @Test
    public void shouldMatch(){
       assertTrue(
            testInstance.isLinkAcceptable("http://brb.to/video/films/i48PZgLVz0TzgLdKAMBKyM8-terminator-3-vosstanije-mashin.html") &&
            testInstance.isLinkAcceptable("http://brb.to/video/serials/igbPc1a1BRRkuY2PuPLnwY-poka-stanica-spit.html")
       );
    }

    @Test
    public void shouldNotMatch(){
        assertFalse(testInstance.isLinkAcceptable("http://brbs.to/video/films/i48PZgLVz0TzgLdKAMBKyM8-terminator-3-vosstanije-mashin.html"));
        assertFalse(testInstance.isLinkAcceptable("https://brb.to/video/serials/igbPc1a1BRRkuY2PuPLnwY-poka-stanica-spit.html"));
        assertFalse(testInstance.isLinkAcceptable("http://brb.to/video/serials/igbPc1a1BRRkuY2PuPLnwY-poka-stanica-spit.html?wew"));
        assertFalse(testInstance.isLinkAcceptable("http://brb.to/video/serials/igbPc1a1BRRkuY2PuPLnwY-poka-stanica-spit.html?wew/asdsad"));
        assertFalse(testInstance.isLinkAcceptable("http://brb.to/video/serials/igbPc1a1BRRkuY2PuPLnwY-poka-stanica-spit.html/asdsad"));
    }

    @Test
    public void shouldParseAMovie() throws ExploreDownloadUrlDefinition.UnreachableUrlException {
        //ExploreDownloadUrlDefinition.DownloadUrlChoice[] choices=testInstance.explore("http://brb.to/video/films/igfsVb9UuVXw89Z3DyHt1S-terminator-2-sudnyj-den.html");
        //assertTrue(choices.length != 0);
    }

    @Test
    public void shouldParseASerial() throws ExploreDownloadUrlDefinition.UnreachableUrlException {
        //ExploreDownloadUrlDefinition.DownloadUrlChoice[] choices=testInstance.explore("http://brb.to/video/cartoonserials/icOK3JoxW0NqRQGSagesM-simpsony.html");
        //assertTrue(choices.length != 0);
    }


    @Test
    public void shouldParseOneSeasonSerial() throws ExploreDownloadUrlDefinition.UnreachableUrlException {
        //ExploreDownloadUrlDefinition.DownloadUrlChoice[] choices=testInstance.explore("http://brb.to/video/serials/igbPc1a1BRRkuY2PuPLnwY-poka-stanica-spit.html");
        //assertTrue(choices.length != 0);
    }
}