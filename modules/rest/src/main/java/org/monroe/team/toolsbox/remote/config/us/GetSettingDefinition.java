package org.monroe.team.toolsbox.remote.config.us;


public interface GetSettingDefinition {

    public String perform(String settingName) throws UnsupportedSettingException;

    public static class UnsupportedSettingException extends Exception{

        public final String settingName;

        public UnsupportedSettingException(String settingName, Throwable cause) {
            super("Unsupported setting = "+settingName, cause);
            this.settingName = settingName;
        }
    }
}
