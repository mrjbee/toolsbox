package org.monroe.team.toolsbox.us;
import org.monroe.team.toolsbox.entities.Storage;

import java.util.List;

public interface GetStoragesDefinition {

    public List<StorageResponse> perform();

    public static class StorageResponse {
        public Integer id;
        public String label;
        public Storage.StorageType type;
        public Integer refFileId;
    }
}
