package org.monroe.team.toolsbox.remote.config.us;

public interface SetSettingDefinition {

    public void perform(SetSettingRequest request) throws NotAllowedSettingException;

    public static interface SetSettingRequest{
        public String getName();
        public String getValue();
    }

    public static class NotAllowedSettingException extends Exception{

        public final String settingName;

        public NotAllowedSettingException(String settingName, Throwable cause) {
            super("No allowed to update setting = "+settingName, cause);
            this.settingName = settingName;
        }
    }
}
