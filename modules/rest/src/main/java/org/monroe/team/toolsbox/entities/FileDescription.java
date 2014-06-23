package org.monroe.team.toolsbox.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.File;

@Entity
public class FileDescription {

    public static enum Type{
        FILE, FOLDER, ROOT
    }


    @Id public Integer id;
    @Column(nullable = false, unique = true)
    public String filePath;
    @Column(nullable = false)
    public Type type;

    public FileDescription() {}


    public String getSimpleName() {
        return new File(filePath).getName();
    }

    public static FileDescription create(File file){
        Type type = file.isDirectory()?Type.FOLDER:Type.FILE;
        return create(file, type);
    }

    public static FileDescription create(File file, Type type) {
        FileDescription answer =new FileDescription();
        answer.filePath = file.getAbsolutePath();
        answer.id = answer.filePath.hashCode();
        answer.type = type;
        return answer;
    }

    public static FileDescription create(String absoluteFilePath) {
        return create(new File(absoluteFilePath));
    }

    public boolean isFolder(){
        return Type.FILE != type;
    }

}
