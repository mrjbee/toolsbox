package org.monroe.team.toolsbox.us;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.monroe.team.toolsbox.entities.FileDescription;
import org.monroe.team.toolsbox.repositories.FileDescriptorRepository;
import org.monroe.team.toolsbox.services.IdTranslator;
import org.monroe.team.toolsbox.us.common.Exceptions;
import org.monroe.team.toolsbox.us.common.FileResponse;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Named
public class GetFileChildren implements GetFileChildrenDefinition{

    @Inject
    FileDescriptorRepository repository;
    @Inject
    IdTranslator idTranslator;

    @Transactional
    @Override
    public List<FileResponse> perform(String parentFileId) throws Exceptions.InvalidIdException {
        Integer parentId = idTranslator.asInt(parentFileId);
        FileDescription parentFileDescription = repository.findOne(parentId);
        if (parentFileDescription == null || parentFileDescription.id == null){
            throw new Exceptions.IdNotFoundException(parentFileId);
        }

        File parentFile = new File(parentFileDescription.filePath);
        if (!parentFile.exists()){
            throw new Exceptions.IdNotFoundException(parentFileId);
        }

        if (!parentFile.isDirectory()){
            return Collections.emptyList();
        }

        File[] childFiles = parentFile.listFiles();
        List<FileDescription> answer = new ArrayList<FileDescription>(childFiles.length);
        for (File childFile : childFiles) {
            if (!childFile.isHidden()) {
                FileDescription childFileDescription = FileDescription.create(childFile);
                repository.save(childFileDescription);
                answer.add(childFileDescription);
            }
        }

        List<FileResponse> list = Lists.newArrayList(Lists.transform(answer,new Function<FileDescription, FileResponse>() {
            @Override
            public FileResponse apply(FileDescription fileDescription) {
                String fileName = fileDescription.getSimpleName();
                FileResponse fileResponse = new FileResponse(fileDescription.id, fileName,
                        fileDescription.isFolder());
                return fileResponse;
            }
        }));

        if (!FileDescription.Type.ROOT.equals(parentFileDescription.type)){
            list.add(new FileResponse(parentFile.getParentFile().getAbsoluteFile().hashCode(),
                    "..",
                    true));
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
