package org.monroe.team.toolsbox.us;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.services.Files;
import org.monroe.team.toolsbox.us.model.StorageModel;
import org.monroe.team.toolsbox.us.model.impl.StorageModelImpl;
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
        List<StorageModel> storageModels = storageManager.getAvailableStorages();
        return Lists.transform(storageModels,new Function<StorageModel, StorageResponse>() {
            @Override
            public StorageResponse apply(StorageModel storageModel) {
                StorageResponse answer = new StorageResponse();
                answer.id = storageModel.getIdentifier();
                answer.label = storageModel.getLabel();
                answer.type = storageModel.getType();
                answer.refFileId = storageModel.getFileRef();
                answer.space = Files.convertToBestUnitsAsString(storageModel.getTotalSpace());
                answer.freeSpace = Files.convertToBestUnitsAsString(storageModel.getFreeSpace());
                return answer;
            }
        });
    }
}
