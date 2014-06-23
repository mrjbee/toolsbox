package org.monroe.team.toolsbox.entities;

public class Storage {

    public Integer id;
    public String label;
    public StorageType type;
    public FileDescription root;

    public Storage() {}

    public Storage(String label, StorageType type, FileDescription file) {
        id = label.hashCode();
        this.label = label;
        this.type = type;
        this.root = file;
    }

    public String getIdAsString() {
        return Integer.toString(id);
    }

    @Override
    public String toString() {
        return "Storage{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", type=" + type +
                ", root=" + root +
                '}';
    }

    public static enum StorageType{
        PORTABLE, PERMANENT;
    }
}
