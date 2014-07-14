package org.monroe.team.toolsbox.transport.rest;

import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.services.ConfigurationManager;
import org.monroe.team.toolsbox.transport.timer.AbstractTimerController;
import org.monroe.team.toolsbox.transport.timer.TimerSchedule;
import org.monroe.team.toolsbox.us.StorageLookupDefinition;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class TimerController extends AbstractTimerController{

    @Inject
    ConfigurationManager configurationManager;

    @Inject
    StorageLookupDefinition storageLookup;

    @Resource(name="task")
    Logger log;

    @TimerSchedule(60000)
    public void lookupStorages(){
        for (ConfigurationManager.StorageLookupConfiguration storageLookupConfiguration : configurationManager.getStorageLookupEntryList()) {
            StorageLookupDefinition.StorageLookupRequest request = new StorageLookupDefinition.StorageLookupRequest(
                    storageLookupConfiguration.filePath,
                    storageLookupConfiguration.scanLevel);
            storageLookup.perform(request);
        }
    }
}
