package org.monroe.team.toolsbox.services.impl;

import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.entities.FileDescription;
import org.monroe.team.toolsbox.repositories.FileDescriptorRepository;
import org.monroe.team.toolsbox.services.StorageManager;
import org.monroe.team.toolsbox.entities.Storage;
import org.springframework.util.Assert;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
public class InMemoryStorageManager implements StorageManager {

    @Inject
    FileDescriptorRepository fileDescriptorRepository;
    private Map<String,Storage> storageMap = new HashMap<String, Storage>();


    @Override
    public Storage save(Storage storage) {
        Assert.notNull(storage);
        fileDescriptorRepository.save(storage.root);
        return storageMap.put(storage.getIdAsString(), storage);
    }

    @Override
    public List<Storage> getAvailableStorages() {
        return Lists.newArrayList(storageMap.values().iterator());
    }
}
