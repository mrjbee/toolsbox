package org.monroe.team.toolsbox.us.model;


import org.monroe.team.toolsbox.us.model.impl.FileModelImpl;
import org.monroe.team.toolsbox.us.model.impl.StorageModelImpl;

public interface StorageModel {

    public String getLabel();
    public StorageType getType();
    public String getIdAsString();
    public int getIdentifier();
    Integer getFileRef();
    String getMountPath();
    boolean isMount();

    FileModel asFileModel();

    Integer getDeviceId();

    int getMaxWriteThreadsCount();

    int getMaxReadThreadsCount();

    double getSpeed();

    void setSpeed(double speed);

    String getSpeedAsString();

    long getTotalSpace();

    long getFreeSpace();

    public static enum StorageType{
        PORTABLE, PERMANENT;
    }
}
