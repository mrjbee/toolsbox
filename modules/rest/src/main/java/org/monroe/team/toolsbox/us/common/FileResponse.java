package org.monroe.team.toolsbox.us.common;

public class FileResponse {

    public final int id;
    public final String name;
    public final boolean folder;
    public final String size;

    public FileResponse(int id, String name, boolean folder, String size) {
        this.id = id;
        this.name = name;
        this.folder = folder;
        this.size = size;
    }
}
