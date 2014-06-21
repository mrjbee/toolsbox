package org.monroe.team.toolsbox.remote.config.us;


import org.monroe.team.toolsbox.remote.config.services.SettingsProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Named
public class UpdateStatusStatistics implements UpdateStatusStatisticsDefinition {

    @Inject
    SettingsProvider settingsProvider;

    @Override
    public void perform(String status) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm [dd-MM-yyyy]");
        Calendar now = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        try {
            settingsProvider.set("lastDate", dateFormat.format(now.getTime()));
            int minutes = settingsProvider.get("sleepminutes",Integer.class);
            String offlineTillDateValue = "NaN";
            if ("Offline".equals(status)){
                now.add(Calendar.MINUTE, minutes);
                offlineTillDateValue = dateFormat.format(now.getTime());
            }
            settingsProvider.set("offlineTillDate", offlineTillDateValue);
        } catch (SettingsProvider.UnsupportedTypeException e) {
            throw new RuntimeException(e);
        }
    }
}
