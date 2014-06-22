package org.monroe.team.toolsbox.us;

public interface StorageLookupDefinition {

    public void perform(StorageLookupRequest request);

    final public static class StorageLookupRequest {

        public final String path;
        public final int level;

        public StorageLookupRequest(String path, int level) {
            this.path = path;
            this.level = level;
        }
    }

}
