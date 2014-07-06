package org.monroe.team.toolsbox.us;
import org.monroe.team.toolsbox.us.model.impl.StorageModelImpl;

import java.util.List;

public interface GetStoragesDefinition {

    public List<StorageResponse> perform();

    public static class StorageResponse {
        public Integer id;
        public String label;
        public StorageModelImpl.StorageType type;
        public Integer refFileId;
        public String space;
        public String freeSpace;
    }
}
