package org.monroe.team.toolsbox.us;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;
import com.google.common.io.Files;
import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.logging.Logs;
import org.monroe.team.toolsbox.services.StorageManager;
import org.monroe.team.toolsbox.us.model.StorageModel;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;


@Named
public class StorageLookup implements StorageLookupDefinition{

    static Logger LOG = Logs.forFeature("Storage");

    @Inject
    StorageManager storageManager;

    @Override
    public void perform(StorageLookupRequest request) {
        LOG.info("Lookup storage at {} using {} scan level", request.path, request.level);
        File scanRoot = new File(request.path);
        if (!scanRoot.exists()){
            LOG.warn("Scan root = {} not exists ", scanRoot.getAbsolutePath());
            return;
        }

        TreeTraverser<File> traverse = Files.fileTreeTraverser();
        FluentIterable<File> iter = traverse.breadthFirstTraversal(scanRoot);
        for(File itFile:iter){
            if (isScanLevelReached(itFile,scanRoot,request.level)) return;
            if (itFile.isFile() && ".storage".equals(itFile.getName())){
                File configFile = itFile;
                StorageModel model = null;
                try {
                    model = storageManager.loadStorageFromFile(configFile);
                    LOG.info("Found storage = {}", model);
                } catch (IOException e) {
                    LOG.warn("Found storage = {} but fails to load", model);
                    LOG.warn(e);
                }
            }
        }
    }


    static boolean isScanLevelReached(File file, File root, int level) {
        File itParent = file.getParentFile();
        for (;level>0;level--){
            if(itParent == null) return false;
            if (itParent.equals(root)) return false;
            itParent = itParent.getParentFile();
        }
        return true;
    }

}
