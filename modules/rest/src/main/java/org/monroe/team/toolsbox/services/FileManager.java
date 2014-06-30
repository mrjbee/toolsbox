package org.monroe.team.toolsbox.services;

import org.monroe.team.toolsbox.us.model.FileModel;
import org.monroe.team.toolsbox.us.model.StorageModel;
import org.monroe.team.toolsbox.us.model.impl.FileModelImpl;

import java.io.File;

public interface FileManager {
    public void linkStorage(StorageModel storage);
    FileModel getById(Integer fileId);
    FileModel mergeByFile(File file, StorageModel storageModel);
    FileModel getParentFor(FileModel fileModel, StorageModel storageModel);
}
