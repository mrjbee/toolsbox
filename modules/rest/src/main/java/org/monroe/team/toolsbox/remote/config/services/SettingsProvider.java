package org.monroe.team.toolsbox.remote.config.services;

public interface SettingsProvider {

    public <ResultType>  ResultType get(String settingName, Class<ResultType> resultType) throws UnsupportedTypeException;
    public <ResultType> void set(String settingName, ResultType value) throws UnsupportedTypeException;

    public static class UnsupportedTypeException extends Exception{

        public UnsupportedTypeException(String settingName, Class type) {
            super("Unsupported type:"+type.getName() +" for setting: "+settingName);
        }
    }
}
