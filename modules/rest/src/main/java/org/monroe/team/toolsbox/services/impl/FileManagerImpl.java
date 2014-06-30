package org.monroe.team.toolsbox.services.impl;

import org.monroe.team.toolsbox.entities.FileDescription;
import org.monroe.team.toolsbox.repositories.FileDescriptorRepository;
import org.monroe.team.toolsbox.services.FileManager;
import org.monroe.team.toolsbox.services.StorageManager;
import org.monroe.team.toolsbox.us.model.FileModel;
import org.monroe.team.toolsbox.us.model.StorageModel;
import org.monroe.team.toolsbox.us.model.impl.FileModelImpl;
import org.monroe.team.toolsbox.us.model.impl.StorageModelImpl;
import org.monroe.team.toolsbox.us.model.impl.dependecy.Dependency;
import org.monroe.team.toolsbox.us.model.impl.dependecy.InMemoryDependency;
import org.monroe.team.toolsbox.us.model.impl.dependecy.JPADependency;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;

@Named
public class FileManagerImpl implements FileManager{

    @Inject
    FileDescriptorRepository fileDescriptorRepository;

    @Inject
    StorageManager storageManager;

    @Override
    public void linkStorage(final StorageModel storage) {
       FileDescription fileDescription = new FileDescription();
       fileDescription.id = storage.getIdentifier();
       fileDescription.filePath="";
       fileDescription.storageId = storage.getIdentifier();
       Dependency<FileDescription> fileDescriptionDependency =
               new JPADependency<FileDescription, Integer>(fileDescriptorRepository,fileDescription.id, fileDescription);
       FileModelImpl fileModel = new FileModelImpl(this,
               fileDescriptionDependency,
               new InMemoryDependency<StorageModel>(new StorageInstanceProvider(fileDescriptionDependency,storageManager)));
       fileModel.save();
       ((StorageModelImpl)storage).initFile(fileModel);
    }

    @Override
    public FileModel getById(Integer fileId) {
        Dependency<FileDescription> fileDescriptionDependency =
                new JPADependency<FileDescription, Integer>(fileDescriptorRepository, fileId);
        Dependency<StorageModel> storageModelDependency = new InMemoryDependency<StorageModel>(
                new StorageInstanceProvider(fileDescriptionDependency,storageManager));
        return new FileModelImpl(this,fileDescriptionDependency, storageModelDependency);
    }

    @Override
    public FileModel mergeByFile(File file, StorageModel storage) {
        String path = storage.getMountPath();
        String filePath = file.getAbsolutePath();
        filePath = filePath.substring(path.length());
        Integer fileId = (storage.getLabel()+filePath).hashCode();

        Dependency<FileDescription> fileDescriptionDependency =
                new JPADependency<FileDescription, Integer>(fileDescriptorRepository,fileId);
        if (!fileDescriptionDependency.exists()){
            FileDescription fileDescription = new FileDescription();
            fileDescription.id = fileId;
            fileDescription.filePath = filePath;
            fileDescription.storageId = storage.getIdentifier();
            fileDescriptionDependency = new JPADependency<FileDescription, Integer>(fileDescriptorRepository,fileId, fileDescription);
            fileDescriptionDependency.save();
        }


        return new FileModelImpl(this,
                fileDescriptionDependency,
                new InMemoryDependency<StorageModel>(
                        new StorageInstanceProvider(fileDescriptionDependency,storageManager)));
    }

    @Override
    public FileModel getParentFor(FileModel fileModel, StorageModel storageModel) {
        File file = fileModel.asFile().getParentFile();
        return mergeByFile(file,storageModel);
    }

    private static class StorageInstanceProvider implements InMemoryDependency.InstanceProvider<StorageModel>{

        private final Dependency<FileDescription> fileDescriptionDependency;
        private final StorageManager storageManager;

        private StorageInstanceProvider(Dependency<FileDescription> fileDescriptionDependency, StorageManager storageManager) {
            this.fileDescriptionDependency = fileDescriptionDependency;
            this.storageManager = storageManager;
        }

        @Override
        public StorageModel get() {
            if (!fileDescriptionDependency.exists()) return null;
            return storageManager.getStorageById(fileDescriptionDependency.get().storageId);
        }
    }
}
