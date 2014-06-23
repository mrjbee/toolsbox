package org.monroe.team.toolsbox.services;

import org.monroe.team.toolsbox.entities.Storage;

import java.util.List;

public interface StorageManager {
    Storage save(Storage storage);
    List<Storage> getAvailableStorages();
}
