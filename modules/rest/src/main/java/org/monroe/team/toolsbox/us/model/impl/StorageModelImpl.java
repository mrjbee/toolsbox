package org.monroe.team.toolsbox.us.model.impl;

import org.monroe.team.toolsbox.services.Files;
import org.monroe.team.toolsbox.us.model.FileModel;
import org.monroe.team.toolsbox.us.model.StorageModel;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StorageModelImpl implements StorageModel {

    private String label;
    private StorageType type;
    private FileModel root;
    private File mntFile;
    private File checkFile;
    private final List<Double> lastSpeedList = new ArrayList<Double>(5);

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

    @Override
    public Integer getDeviceId() {
        return getType().equals(StorageType.PERMANENT)?1:0;
    }

    @Override
    public int getMaxWriteThreadsCount() {
        return getType().equals(StorageType.PERMANENT)?1:1;
    }

    @Override
    public int getMaxReadThreadsCount() {
        return getType().equals(StorageType.PERMANENT)?2:1;
    }

    @Override
    public synchronized double getSpeed() {
        if (lastSpeedList.isEmpty()) return 0;
        double averageSpeed = 0;
        for (Double speed : lastSpeedList) {
            averageSpeed += speed;
        }
        return averageSpeed/ lastSpeedList.size();
    }

    @Override
    public synchronized void setSpeed(double speed) {
        lastSpeedList.add(speed);
        if (lastSpeedList.size() > 5){
            lastSpeedList.remove((int)0);
        }
    }

    @Override
    public String getSpeedAsString() {
        if (getSpeed() == 0) return "Undefined";
        double bytesInMs = getSpeed();
        double mByteInMin = bytesInMs * 1000 / Files.convertFromUnits(1, Files.Units.Megabyte);
        return new DecimalFormat("##.##").format(mByteInMin)+" mb/sec";
    }

    @Override
    public long getFreeSpace() {
        return (isMount())?asFileModel().asFile().getUsableSpace():0;
    }

    @Override
    public long getTotalSpace() {
        return (isMount())?asFileModel().asFile().getTotalSpace():0;
    }

    public void merge(StorageModelImpl storageModel) {
        this.type = storageModel.type;
        this.mntFile = storageModel.mntFile;
        this.checkFile = storageModel.checkFile;
    }
}
