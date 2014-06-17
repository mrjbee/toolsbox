package org.monroe.team.toolsbox.remote.config.services.impl;

import org.monroe.team.toolsbox.remote.config.services.SettingsProvider;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class InMemorySettingsProvider implements SettingsProvider{

    private final Map<String, SettingValue> settingValueMap = new HashMap<String, SettingValue>();

    public InMemorySettingsProvider() {
        settingValueMap.put("sleepminutes", new IntegerSettingValue(60));
    }

    @Override
    public <ResultType> ResultType get(String settingName, Class<ResultType> resultType) throws UnsupportedTypeException {
        SettingValue value = settingValueMap.get(settingName);
        if (value == null){
            value = createDefaultValueFor(settingName);
            if (value == null) throw new NullPointerException("No setting found:"+settingName);
            settingValueMap.put(settingName,value);
        }
        if (!value.isSupportedForGet(resultType)){
            throw new UnsupportedTypeException(settingName, resultType);
        }
        return value.get(resultType);
    }

    @Override
    public <ResultType> void set(String settingName, ResultType newValue) throws UnsupportedTypeException {
        SettingValue value = settingValueMap.get(settingName);
        if (value == null){
            value = createDefaultValueFor(settingName);
            if (value == null) throw new NullPointerException("No setting found:"+settingName);
            settingValueMap.put(settingName,value);
        }
        if (!value.isSupportedForSet(newValue.getClass())){
            throw new UnsupportedTypeException(settingName, newValue.getClass());
        }
        value.set(newValue,newValue.getClass());
    }

    protected SettingValue createDefaultValueFor(String settingName) {
        return new StringSettingValue("NaN");
    }

    static private interface SettingValue {
        public boolean isSupportedForSet(Class<?> requiredClass);
        public boolean isSupportedForGet(Class<?> requiredClass);
        public <ResultType> ResultType get( Class<ResultType> resultType);
        public <ResultType> void set(Object value, Class<ResultType> resultType);
    }

    static protected class StringSettingValue implements SettingValue {

        private String value;

        public StringSettingValue(String value) {
            this.value = value;
        }

        @Override
        public boolean isSupportedForSet(Class<?> requiredClass) {
            return String.class.equals(requiredClass);
        }

        @Override
        public boolean isSupportedForGet(Class<?> requiredClass) {
            return String.class.equals(requiredClass);
        }

        @Override
        public <ResultType> ResultType get(Class<ResultType> resultType) {
            return (ResultType) value;
        }

        @Override
        public <ResultType> void set(Object value, Class<ResultType> resultType) {
            this.value = (String) value;
        }
    }

    static protected class IntegerSettingValue implements SettingValue {

        private Integer value;

        public IntegerSettingValue(Integer value) {
            this.value = value;
        }

        @Override
        public boolean isSupportedForSet(Class<?> requiredClass) {
            return String.class.equals(requiredClass) || Integer.class.equals(requiredClass);
        }

        @Override
        public boolean isSupportedForGet(Class<?> requiredClass) {
            return String.class.equals(requiredClass) || Integer.class.equals(requiredClass);
        }

        @Override
        public <ResultType> ResultType get(Class<ResultType> resultType) {
            if (String.class.equals(resultType)){
                return (ResultType) Integer.toString(value);
            } else if (Integer.class.equals(value)){
                return (ResultType) value;
            } else {
                throw new IllegalStateException("Unexpected class");
            }
        }

        @Override
        public <ResultType> void set(Object value, Class<ResultType> resultType) {
            if (String.class.equals(resultType)){
                this.value = Integer.parseInt((String) value);
            } else if (Integer.class.equals(value)){
                this.value = (Integer) value;
            } else {
                throw new IllegalStateException("Unexpected class");
            }
        }
    }

}
