package org.monroe.team.toolsbox.transport.rest;


import org.monroe.team.toolsbox.us.GetFileChildrenDefinition;
import org.monroe.team.toolsbox.us.GetStoragesDefinition;
import org.monroe.team.toolsbox.us.common.FileResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

@Controller
public class FilesController {

    @Inject GetFileChildrenDefinition getFileChildren;
    @Inject GetStoragesDefinition getStorages;

    @RequestMapping("/file/{fileId}/children")
    public @ResponseBody List<FileResponse> getFileChildren(@PathVariable String fileId){
        return getFileChildren.perform(fileId);
    }

    @RequestMapping("/storages")
    public @ResponseBody List<GetStoragesDefinition.StorageResponse> getStorages(){
        return getStorages.perform();
    }
}
