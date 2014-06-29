package org.monroe.team.toolsbox.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.File;

@Entity
public class FileDescription {

    @Id public Integer id;

    @Column(nullable = false)
    public String filePath;

    @Column (nullable = false)
    public Integer storageId;

    public FileDescription() {}


    public String getSimpleName() {
        if (filePath.isEmpty())return ".";
        return new File(filePath).getName();
    }

}
