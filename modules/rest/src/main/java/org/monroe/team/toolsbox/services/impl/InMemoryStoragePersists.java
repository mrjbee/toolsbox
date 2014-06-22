package org.monroe.team.toolsbox.services.impl;

import org.monroe.team.toolsbox.services.StoragePersist;
import org.monroe.team.toolsbox.entities.Storage;
import org.springframework.util.Assert;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class InMemoryStoragePersists implements StoragePersist {

    private Map<String,Storage> storageMap = new HashMap<String, Storage>();

    @Override
    public Storage save(Storage storage) {
        Assert.notNull(storage);
        return storageMap.put(storage.getIdAsString(), storage);
    }
}
