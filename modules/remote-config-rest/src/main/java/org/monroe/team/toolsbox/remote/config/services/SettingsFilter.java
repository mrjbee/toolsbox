package org.monroe.team.toolsbox.remote.config.services;

public interface SettingsFilter {
    public boolean allowedToGet(String name);
    public boolean allowedToSet(String name);
}
