package org.monroe.team.toolsbox.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.monroe.team.toolsbox.services.ConfigurationManager;

import java.io.File;

import static org.junit.Assert.*;

public class ConfigurationManagerImplTest {

    ConfigurationManagerImpl testInstance = new ConfigurationManagerImpl();

    @Before public void before(){
        clenupFile();
    }

    @After public void after(){
       // clenupFile();
    }

    private void clenupFile() {
        new File(ConfigurationManagerImpl.configurationFilPath).delete();
    }

    @Test
    public void checkConfigurationIO(){
        assertNull(testInstance.configuration);
        ConfigurationManager.StorageLookupConfiguration lookupConfiguration = new ConfigurationManager.StorageLookupConfiguration("/mnt",3);
        ConfigurationManager.Configuration configuration = new ConfigurationManager.Configuration(lookupConfiguration);
        testInstance.setConfig(configuration);
        assertTrue(configuration == testInstance.configuration);
        testInstance.configuration = null;
        testInstance.loadLocalFile();
        assertEquals(testInstance.configuration.storageLookupConfigurations[0].filePath, "/mnt");

    }

}