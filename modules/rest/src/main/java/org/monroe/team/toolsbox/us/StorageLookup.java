package org.monroe.team.toolsbox.us;

import com.google.common.base.Charsets;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;
import com.google.common.io.Files;
import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.logging.Logs;
import org.monroe.team.toolsbox.services.StoragePersist;
import org.monroe.team.toolsbox.entities.Storage;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Named
public class StorageLookup implements StorageLookupDefinition{

    static Logger LOG = Logs.forFeature("Storage");

    @Inject StoragePersist storagePersist;

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
                try {
                    Storage storage = loadStorage(configFile);
                    storagePersist.save(storage);
                    LOG.info("Found storage = {}", storage);
                } catch (IOException e) {
                    LOG.warn("Couldn`t load store = {}: {}", configFile.getAbsolutePath());
                    LOG.warn(e);
                }
            }
        }
    }

    private Storage loadStorage(File configFile) throws IOException {
        Properties properties = new Properties();
        properties.load(Files.newReader(configFile, Charsets.UTF_8));
        Storage.StorageType type = Storage.StorageType.valueOf(properties.getProperty("type",
                Storage.StorageType.PERMANENT.name()));
        String label = properties.getProperty("label", "undefined");
        return new Storage(label, configFile.getParentFile().getAbsolutePath(), type);
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
