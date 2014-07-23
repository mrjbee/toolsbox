package org.monroe.team.toolsbox.us.model;

import com.google.common.base.Function;
import org.monroe.team.toolsbox.us.model.impl.FileModelImpl;

import java.io.*;
import java.util.List;

public interface FileModel {
    String getSimpleName();
    String getPath();
    Integer getRef();
    boolean isExistsLocally();
    boolean isDirectory();
    <ResultType> List<ResultType> forEachChild(Function<FileModel, ResultType> function);
    boolean isStorageRoot();
    boolean same(FileModel fileModel);

    FileModel getParent();

    File asFile();

    boolean isHidden();

    StorageModel getStorage();

    FileModel createFile(String simpleName);

    void remove() throws IOException;

    OutputStream openWriteStream() throws IOException;

    InputStream openReadStream() throws IOException;

    long getByteSize();

    void delete();

    FileModel renameTo(String fileName);

    boolean storageExists();

    public boolean isStorageMounted();
}
