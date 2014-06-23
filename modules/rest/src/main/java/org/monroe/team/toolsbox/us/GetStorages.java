package org.monroe.team.toolsbox.us;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.entities.Storage;
import org.monroe.team.toolsbox.services.StorageManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class GetStorages implements GetStoragesDefinition{

    @Inject
    StorageManager storageManager;

    @Override
    public List<StorageResponse> perform() {
        List<Storage> storages = storageManager.getAvailableStorages();
        return Lists.transform(storages,new Function<Storage, StorageResponse>() {
            @Override
            public StorageResponse apply(Storage storage) {
                StorageResponse answer = new StorageResponse();
                answer.id = storage.id;
                answer.label =storage.label;
                answer.type =storage.type;
                answer.refFileId =storage.root.id;
                return answer;
            }
        });
    }
}
