package org.monroe.team.toolsbox.remote.config.us;

import org.monroe.team.toolsbox.remote.config.services.SettingsFilter;
import org.monroe.team.toolsbox.remote.config.services.SettingsProvider;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class GetSetting implements GetSettingDefinition {

    @Inject SettingsProvider settingsProvider;
    @Inject SettingsFilter settingsFilter;

    @Override
    public String perform(String settingName) throws UnsupportedSettingException {

        if (!settingsFilter.allowedToGet(settingName))
            throw new UnsupportedSettingException(settingName, null);

        try {
            return settingsProvider.get(settingName, String.class);
        } catch (SettingsProvider.UnsupportedTypeException e) {
            throw new RuntimeException("Something goes wrong, while getting:"+settingName, e);
        }
    };
}
