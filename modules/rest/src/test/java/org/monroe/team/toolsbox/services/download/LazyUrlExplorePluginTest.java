package org.monroe.team.toolsbox.services.download;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LazyUrlExplorePluginTest {

    LazyUrlExplorePlugin testInstance;

    @Before
    public void initTestIInstance(){
        testInstance = new LazyUrlExplorePlugin();
    }

    @Test
    public void shouldMatch(){
        assertTrue(testInstance.isLinkAcceptable("plugin:fs/1232/34"));
    }

    @Test
    public void shouldNotMatch(){
        assertFalse(testInstance.isLinkAcceptable("http://plugin:fs/1232"));
    }

    @Test
    public void shouldExtractIdAndPluginName(){
        String[] pluginNamePerId = testInstance.parseUrl("plugin:fs/1234/64");
        assertEquals("fs",pluginNamePerId[0]);
        assertEquals("1234",pluginNamePerId[1]);
    }
    @Test
    public void shouldExtractIdAndDifferentPluginName(){
        String[] pluginNamePerId = testInstance.parseUrl("plugin:youtube/2/53");
        assertEquals("youtube",pluginNamePerId[0]);
        assertEquals("2",pluginNamePerId[1]);
        assertEquals("53",pluginNamePerId[2]);
    }
}