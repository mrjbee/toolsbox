package org.monroe.team.toolsbox.services;

import org.monroe.team.toolsbox.us.model.StorageModel;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface StorageManager {
    List<StorageModel> getAvailableStorages();
    StorageModel loadStorageFromFile(File configFile) throws IOException;
    StorageModel getStorageById(Integer storageId);
}
