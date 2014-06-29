package org.monroe.team.toolsbox.us.model;


import org.monroe.team.toolsbox.us.model.impl.FileModelImpl;

public interface StorageModel {

    public String getLabel();
    public StorageType getType();
    public String getIdAsString();
    public int getIdentifier();
    Integer getFileRef();
    String getMountPath();
    boolean isMount();

    FileModel asFileModel();


    public static enum StorageType{
        PORTABLE, PERMANENT;
    }
}
