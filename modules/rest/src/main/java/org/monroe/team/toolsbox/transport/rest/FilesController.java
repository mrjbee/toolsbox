package org.monroe.team.toolsbox.transport.rest;


import org.monroe.team.toolsbox.transport.Translator;
import org.monroe.team.toolsbox.transport.TransportExceptions;
import org.monroe.team.toolsbox.us.*;
import org.monroe.team.toolsbox.us.common.BusinessExceptions;
import org.monroe.team.toolsbox.us.common.FileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Controller
public class FilesController {

    @Inject GetFileChildrenDefinition getFileChildren;
    @Inject GetStoragesDefinition getStorages;
    @Inject DeleteFileDefinition deleteFile;
    @Inject RenameFileDefinition renameFile;
    @Inject CreateFolderDefinition createFolder;
    @Inject Translator translate;

    @RequestMapping("/file/{fileId}/children")
    public @ResponseBody List<FileResponse> getFileChildren(@PathVariable String fileId){
        return getFileChildren.perform(fileId);
    }

    @RequestMapping(value = "/file/{fileId}",method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteFile(@PathVariable String fileId){
        Integer Id = translate.toIntegerId(fileId);
        try {
            deleteFile.perform(Id);
        } catch (BusinessExceptions.FileOperationFailException e) {
            throw new TransportExceptions.InvalidOperationException("fails", "Operation fails", e);
        } catch (BusinessExceptions.FileNotFoundException e) {
            throw new TransportExceptions.IdNotFoundException(fileId);
        }
    }

    @RequestMapping(value = "/file/{fileId}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody FileResponse changeFile(@PathVariable String fileId, @RequestBody Map details){
        Integer id = translate.toIntegerId(fileId);
        if (details.containsKey("name")){
            try {
                renameFile.perform(new RenameFileDefinition.FileRenameRequest(id,(String)details.get("name")));
            } catch (BusinessExceptions.FileOperationFailException e) {
                throw new TransportExceptions.InvalidOperationException("fails", "Operation fails", e);
            } catch (BusinessExceptions.FileNotFoundException e) {
                throw new TransportExceptions.IdNotFoundException(fileId);
            }
        }else {
            throw new TransportExceptions.InvalidRequestException(new RuntimeException("Unsupported change"));
        }
        return null;
    }

    @RequestMapping(value = "/file", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody FileResponse createDir(@RequestBody CreateFolderDefinition.CreateFolderRequest request){
        try {
            return createFolder.perform(request);
        } catch (BusinessExceptions.FileOperationFailException e) {
            throw new TransportExceptions.InvalidOperationException("fails", "Operation fails", e);
        } catch (BusinessExceptions.FileNotFoundException e) {
            throw new TransportExceptions.IdNotFoundException(request.name);
        }
    }


    @RequestMapping("/storages")
    public @ResponseBody List<GetStoragesDefinition.StorageResponse> getStorages(){
        return getStorages.perform();
    }
}
