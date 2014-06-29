package org.monroe.team.toolsbox.us.model.impl;

import org.monroe.team.toolsbox.us.model.FileModel;
import org.monroe.team.toolsbox.us.model.StorageModel;

import java.io.File;

public class StorageModelImpl implements StorageModel {

    private String label;
    private StorageType type;
    private FileModel root;
    private File mntFile;
    private File checkFile;

    public StorageModelImpl(String label, StorageType type, File checkFile) {
        this.label = label;
        this.type = type;
        this.mntFile = checkFile.getParentFile();
        this.checkFile = checkFile;
    }

    public void initFile(FileModel fileModel){
        this.root= fileModel;
    };

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public StorageType getType() {
        return type;
    }

    public String getIdAsString() {
        return Integer.toString(getIdentifier());
    }

    public int getIdentifier() {
        return label.hashCode();
    }

    @Override
    public String toString() {
        return "Storage{" +
                "id=" + getIdentifier() +
                ", label='" + label + '\'' +
                ", type=" + type +
                ", root=" + getMountPath() +
                '}';
    }

    public Integer getFileRef() {
        return root.getRef();
    }

    @Override
    public String getMountPath() {
        return mntFile.getAbsolutePath();
    }

    @Override
    public boolean isMount() {
        return checkFile!=null && checkFile.exists();
    }

    @Override
    public FileModel asFileModel() {
        return root;
    }
}
