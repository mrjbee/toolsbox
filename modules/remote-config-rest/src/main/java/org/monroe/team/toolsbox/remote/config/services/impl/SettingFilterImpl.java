package org.monroe.team.toolsbox.remote.config.services.impl;

import org.monroe.team.toolsbox.remote.config.services.SettingsFilter;

import javax.inject.Named;

@Named
public class SettingFilterImpl implements SettingsFilter {

    private final String[] allowedSettingsToGet = {"sleepminutes","status","offlineTillDate","lastDate"};
    private final String[] allowedSettingsToSet = {"sleepminutes","status"};

    @Override
    public boolean allowedToGet(String name) {
        return existInList(name, allowedSettingsToGet);
    }

    @Override
    public boolean allowedToSet(String name) {
        return existInList(name, allowedSettingsToSet);
    }

    private boolean existInList(String name, String[] arrayToCheck) {
        for (String allowedSetting : arrayToCheck) {
            if (allowedSetting.equals(name)) return true;
        }
        return false;
    }

}
