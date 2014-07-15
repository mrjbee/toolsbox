package org.monroe.team.toolsbox.entities;

public interface Execution {
    public Float getProgress();
    public Object getStatistic(String key);
    void kill();
}
