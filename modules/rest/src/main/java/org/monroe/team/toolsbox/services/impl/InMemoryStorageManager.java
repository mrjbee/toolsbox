package org.monroe.team.toolsbox.services.impl;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.monroe.team.toolsbox.services.FileManager;
import org.monroe.team.toolsbox.services.StorageManager;
import org.monroe.team.toolsbox.us.model.StorageModel;
import org.monroe.team.toolsbox.us.model.impl.StorageModelImpl;
import org.springframework.util.Assert;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Named
public class InMemoryStorageManager implements StorageManager {

    @Inject
    FileManager fileManager;
    private Map<String, StorageModel> storageMap = new HashMap<String, StorageModel>();

    @Override
    public List<StorageModel> getAvailableStorages() {
        return Lists.newArrayList(storageMap.values().iterator());
    }

    @Override
    public StorageModel loadStorageFromFile(File configFile) throws IOException {
        Properties properties = loadStorageProperties(configFile);
        StorageModel.StorageType type = StorageModel.StorageType.valueOf(properties.getProperty("type", StorageModelImpl.StorageType.PERMANENT.name()));
        String label = properties.getProperty("label", null);
        Assert.notNull(label);
        StorageModelImpl storageModel = new StorageModelImpl(label, type, configFile);
        if (storageMap.get(storageModel.getIdAsString())!= null){
            ((StorageModelImpl)storageMap.get(storageModel.getIdAsString())).merge(storageModel);
            storageModel = (StorageModelImpl) storageMap.get(storageModel.getIdAsString());
        } else {
            fileManager.linkStorage(storageModel);
            storageMap.put(storageModel.getIdAsString(), storageModel);
        }
        return storageModel;
    }

    @Override
    public StorageModel getStorageById(Integer storageId) {
        return storageMap.get(storageId.toString());
    }

    private Properties loadStorageProperties(File configFile) throws IOException {
        Properties properties = new Properties();
        properties.load(Files.newReader(configFile, Charsets.UTF_8));
        return properties;
    }

}
