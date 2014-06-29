package org.monroe.team.toolsbox.us;

import org.junit.Test;
import org.monroe.team.toolsbox.us.StorageLookup;

import java.io.File;

import static org.junit.Assert.*;

public class StorageModelLookupTest {

    final static File testFile = new File("").getAbsoluteFile();

    @Test
    public void shouldRichScanLevelWithScanLevel1(){
        assertTrue(StorageLookup.isScanLevelReached(testFile, testFile.getParentFile().getParentFile(), 1));
    }

    @Test
    public void shouldNotRichScanLevelWithScanLevel1(){
        assertFalse(StorageLookup.isScanLevelReached(testFile, testFile.getParentFile(), 1));
    }

    @Test
    public void shouldRichScanLevelWithScanLevel3(){
        assertTrue(StorageLookup.isScanLevelReached(
                testFile,
                testFile.getParentFile().getParentFile().getParentFile(),
                1));
    }

    @Test
    public void shouldNotRichScanLevelWithScanLevel3(){
        assertFalse(StorageLookup.isScanLevelReached(
                testFile,
                testFile.getParentFile().getParentFile(),
                3));
    }

    @Test
    public void shouldRichScanLevelWithScanLevel0Immediately(){
        assertTrue(StorageLookup.isScanLevelReached(
                testFile,
                testFile,
                0));
    }

}