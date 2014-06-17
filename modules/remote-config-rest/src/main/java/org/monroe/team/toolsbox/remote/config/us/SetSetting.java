package org.monroe.team.toolsbox.remote.config.us;

import org.monroe.team.toolsbox.remote.config.services.SettingsFilter;
import org.monroe.team.toolsbox.remote.config.services.SettingsProvider;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class SetSetting implements SetSettingDefinition{

    @Inject SettingsProvider settingsProvider;
    @Inject SettingsFilter settingsFilter;

    @Override
    public void perform(SetSettingRequest request) throws NotAllowedSettingException {
        if (!settingsFilter.allowedToSet(request.getName())){
            throw new NotAllowedSettingException(request.getName(), null);
        }
        try {
            settingsProvider.set(request.getName(), request.getValue());
        } catch (SettingsProvider.UnsupportedTypeException e) {
            throw new RuntimeException("Oops! Something wrong with updating = "+request.getName(), e);
        }
    }
}
