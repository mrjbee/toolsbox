package org.monroe.team.toolsbox.us;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.repositories.FileDescriptorRepository;
import org.monroe.team.toolsbox.services.FileManager;
import org.monroe.team.toolsbox.services.Files;
import org.monroe.team.toolsbox.services.IdTranslator;
import org.monroe.team.toolsbox.transport.TransportExceptions;
import org.monroe.team.toolsbox.us.common.FileResponse;
import org.monroe.team.toolsbox.us.model.FileModel;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Named
public class GetFileChildren implements GetFileChildrenDefinition{

    @Inject
    FileManager fileManager;

    @Inject
    FileDescriptorRepository repository;

    @Inject
    IdTranslator idTranslator;

    @Transactional
    @Override
    public List<FileResponse> perform(String parentFileId) throws TransportExceptions.InvalidIdException {
        Integer parentId = idTranslator.asInt(parentFileId);
        FileModel parentFile = fileManager.getById(parentId);
        if (!parentFile.isExistsLocally()){
            throw new TransportExceptions.IdNotFoundException(parentFileId);
        }

        if (!parentFile.isDirectory()){
            return Collections.emptyList();
        }

        List<FileResponse> list =
             Lists.newArrayList(Iterables.filter(
                parentFile.forEachChild(new Function<FileModel, FileResponse>() {
                    @Override
                    public FileResponse apply(FileModel fileModel) {
                        if (fileModel.isHidden()) return null;
                        String fileName = fileModel.getSimpleName();
                        FileResponse fileResponse = new FileResponse(
                                fileModel.getRef(),
                                fileName,
                                fileModel.isDirectory(),
                                Files.convertToBestUnitsAsString(fileModel.getByteSize()));
                        return fileResponse;
                    }
                }), new Predicate<FileResponse>() {
                    @Override
                    public boolean apply(FileResponse fileResponse) {
                        return fileResponse != null;
                    }
             }));


        if (!parentFile.isStorageRoot()){
            list.add(new FileResponse(
                    parentFile.getParent().getRef(),
                    "..",
                    true,
                    Files.convertToUnitsAsString(0, Files.Units.Megabyte)));
        }

        Collections.sort(list, new Comparator<FileResponse>() {
            @Override
            public int compare(FileResponse o1, FileResponse o2) {
                if (o1.name.equals("..")) return -1;
                if (o2.name.equals("..")) return 1;
                if (o1.folder && !o2.folder) return -1;
                if (!o1.folder && o2.folder) return 1;
                return o1.name.compareTo(o2.name);
            }
        });

        return list;
    }
}
