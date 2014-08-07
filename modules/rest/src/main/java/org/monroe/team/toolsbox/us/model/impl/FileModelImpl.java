package org.monroe.team.toolsbox.us.model.impl;


import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.entities.FileDescription;
import org.monroe.team.toolsbox.services.FileManager;
import org.monroe.team.toolsbox.us.model.FileModel;
import org.monroe.team.toolsbox.us.model.StorageModel;
import org.monroe.team.toolsbox.us.model.impl.dependecy.Dependency;

import java.io.*;
import java.util.List;

public class FileModelImpl implements FileModel{

    private FileManager fileManager;
    private final Dependency<FileDescription> fileDescriptionDependency;
    private final Dependency<StorageModel> storageModelDependency;

    public FileModelImpl(FileManager fileManager, Dependency<FileDescription> fileDescriptionDependency, Dependency<StorageModel> storageModelDependency) {
        this.fileDescriptionDependency = fileDescriptionDependency;
        this.storageModelDependency = storageModelDependency;
        this.fileManager = fileManager;
    }

    public void save() {
        fileDescriptionDependency.save();
    }

    @Override
    public String getSimpleName() {
        check(isHealthy());
        return fileDescriptionDependency.get().getSimpleName();
    }

    public String getPath() {
        check(isHealthy());
        return storageModelDependency.get().getMountPath() + File.separator+fileDescriptionDependency.get().filePath;
    }

    @Override
    public Integer getRef() {
        check(isHealthy());
        return fileDescriptionDependency.get().id;
    }

    @Override
    public boolean isExistsLocally() {
        return isExists(null);
    }


    @Override
    public boolean isDirectory() {
        check(isStorageMounted());
        return new File(getPath()).isDirectory();
    }

    @Override
    public <ResultType> List<ResultType> forEachChild(Function<FileModel, ResultType> function) {
        FileRequest request = new FileRequest();
        check(isExists(request));
        List<FileModel> childList = Lists.transform(Lists.newArrayList(request.file.listFiles()), new Function<File, FileModel>() {
            @Override
            public FileModel apply(File file) {
                return fileManager.mergeByFile(file, storageModelDependency.get());
            }
        });
        return Lists.transform(childList, function);
    }

    @Override
    public boolean isStorageRoot() {
        check(isHealthy());
        return storageModelDependency.get().asFileModel().same(this);
    }

    @Override
    public boolean same(FileModel fileModel) {
        check(isHealthy());
        return fileModel.getRef().equals(this.getRef());
    }

    @Override
    public FileModel getParent() {
        check(isHealthy());
        return fileManager.getParentFor(this, storageModelDependency.get());
    }

    @Override
    public File asFile() {
        check(storageModelDependency.exists());
        check(fileDescriptionDependency.exists());
        return new File(getPath());
    }

    @Override
    public boolean isHidden() {
        FileRequest fileRequest = new FileRequest();
        check(isExists(fileRequest));
        return fileRequest.file.isHidden();
    }

    @Override
    public StorageModel getStorage() {
        check(isHealthy());
        return storageModelDependency.get();
    }

    @Override
    public FileModel createFile(String simpleName) {
        check(isHealthy());
        return fileManager.mergeByFile(createLocalFileWithName(simpleName),storageModelDependency.get());
    }

    @Override
    public void remove() throws IOException {
        check(isHealthy());
        if (!deleteFile(asFile())){
            throw new IOException("File could`n be deleted. = "+asFile().getAbsolutePath());
        }
    }

    @Override
    public OutputStream openWriteStream() throws IOException {
        return new FileOutputStream(asFile());
    }

    @Override
    public InputStream openReadStream() throws IOException {
        return new FileInputStream(asFile());
    }

    @Override
    public long getByteSize() {
        return (isDirectory()) ? 0:asFile().length();
    }

    @Override
    public void delete() {
        if (!isExistsLocally()) throw new RuntimeException("File not found");

        if(isStorageRoot()){
            throw new RuntimeException("Couldn`t remove storage root");
        }

        if(!deleteFile(asFile())){
            throw new RuntimeException("Couldn`t delete file");
        }
    }

    private boolean deleteFile(File file) {

        if (!file.isDirectory()){
            return file.delete();
        }

        File[] childFiles = file.listFiles();
        boolean success = true;
        for (File childFile : childFiles) {
            success = success && deleteFile(childFile);
        }
        return success && file.delete();
    }

    @Override
    public FileModel renameTo(String fileName) {
        if (!isExistsLocally()) throw new RuntimeException("File not found");
        if (isStorageRoot()) throw new RuntimeException("Couldn`t rename storage root");
        FileModel newFile = getParent().createFile(fileName);
        if(!asFile().renameTo(newFile.asFile())){
            throw new RuntimeException("Couldn`t delete file");
        }
        return newFile;
    }

    @Override
    public boolean storageExists() {
        return storageModelDependency.get() != null;
    }

    private File createLocalFileWithName(String simpleName) {
        return new File(asFile(), simpleName);
    }

    private boolean isExists(FileRequest request) {
        if (!isStorageMounted()) return false;
        File file = new File(getPath());
        if (request != null){
            request.file = file;
        }
        return file.exists();
    }

    private void check(boolean condition) {
        if (!condition){
            throw new IllegalStateException();
        }
    }

    public boolean isStorageMounted() {
        return isHealthy() &&
                storageModelDependency.get().isMount();
    }

    private boolean isHealthy() {
        return storageModelDependency.exists() &&
                fileDescriptionDependency.exists();
    }

    private class FileRequest{
        public File file;
    }

}
