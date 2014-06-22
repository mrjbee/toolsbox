package org.monroe.team.toolsbox.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Storage {

    @Id
    public Integer id;
    @Column(unique = true, nullable = false)
    public String label;
    @Column(unique = true, nullable = false)
    public String rootPath;
    @Column(nullable = false)
    public StorageType type;

    public Storage() {}

    public Storage(String label, String rootPath, StorageType type) {
        this.label = label;
        id = rootPath.hashCode();
        this.rootPath = rootPath;
        this.type = type;
    }

    public String getIdAsString() {
        return Integer.toString(id);
    }

    @Override
    public String toString() {
        return "Storage{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", rootPath='" + rootPath + '\'' +
                ", type=" + type +
                '}';
    }

    public static enum StorageType{
        PORTABLE, PERMANENT;
    }
}
