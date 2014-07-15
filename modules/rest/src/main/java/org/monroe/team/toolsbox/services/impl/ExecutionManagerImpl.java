package org.monroe.team.toolsbox.services.impl;

import com.google.common.base.Function;
import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.entities.Execution;
import org.monroe.team.toolsbox.services.ExecutionManager;
import org.monroe.team.toolsbox.services.Files;
import org.monroe.team.toolsbox.us.model.FileModel;
import org.monroe.team.toolsbox.us.model.StorageModel;
import org.monroe.team.toolsbox.us.model.TaskModel;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Named
public class ExecutionManagerImpl implements ExecutionManager{

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Map <Integer, Execution> currentExecutionsMap = new HashMap<Integer, Execution>();
    private Map <Integer, Integer> readThreadsPerDeviceMap = new HashMap<Integer, Integer>();
    private Map <Integer, Integer> writeThreadsPerDeviceMap = new HashMap<Integer, Integer>();

    @Resource(name="task") Logger taskLog;
    @Resource(name="core") Logger logCore;

    @PreDestroy
    public void shutdown(){
        logCore.info("Execution manager graceful shutdown started.");
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logCore.warn("Execution manager graceful shutdown was interrupted.", e);
        }
    }

    @Override
    public Execution getTaskExecution(Integer taskId) {
        taskLog.debug("[Task = {}] Requested execution. Exists = {}", taskId, currentExecutionsMap.get(taskId));
        return currentExecutionsMap.get(taskId);
    }

    @Override
    public synchronized void executeAsCopyTask(final TaskModel taskModel, boolean restart) throws ExecutionUnavailableException {
        final FileModel srcFile = taskModel.getProperty("src", FileModel.class);
        final FileModel dstFile = taskModel.getProperty("dst", FileModel.class);
        if (!srcFile.isExistsLocally() || !dstFile.isExistsLocally()) {
            throw new ExecutionUnavailableException(ExecutionUnavailableException.Reason.no_file);
        }
        if (isDevicesBusyForNewRead(srcFile.getStorage()) || isDevicesBusyForNewWrite(dstFile.getStorage())){
            throw new ExecutionUnavailableException(ExecutionUnavailableException.Reason.device_is_busy);
        }

        final int readDeviceId = srcFile.getStorage().getDeviceId();
        final int writeDeviceId = dstFile.getStorage().getDeviceId();

        final BaseExecution execution = new CopyExecution(new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                taskLog.info("Free resources for task = {}", taskModel.getRef());
                freeThreads(readDeviceId, writeDeviceId);
                currentExecutionsMap.remove(taskModel.getRef());
                return null;
            }
        },taskModel, !restart, taskLog);
        taskLog.info("Capture resources for task = {}", taskModel.getRef());
        captureThreads(readDeviceId, writeDeviceId);
        execution.initialize();
        taskLog.info("Registering execution of task = {} :"+this.toString(), taskModel.getRef());
        currentExecutionsMap.put(taskModel.getRef(),execution);
        taskModel.updateStatus(TaskModel.ExecutionStatus.Progress);
        executorService.execute(execution);
    }

    private synchronized void freeThreads(int readDeviceId, int writeDeviceId) {
        indexThreads(readDeviceId, readThreadsPerDeviceMap, -1);
        indexThreads(writeDeviceId, writeThreadsPerDeviceMap, -1);
    }

    private synchronized void captureThreads(int readFromStorage, int writeToStorage) {
        indexThreads(readFromStorage, readThreadsPerDeviceMap, 1);
        indexThreads(writeToStorage, writeThreadsPerDeviceMap, 1);
    }

    private void indexThreads(int deviceId, Map<Integer, Integer> threadPerDeviceMap, int delta) {
        Integer existsCount = threadPerDeviceMap.get(deviceId);
        if (existsCount == null) existsCount = new Integer(0);
        threadPerDeviceMap.put(deviceId, existsCount+delta);
    }


    private synchronized boolean isDevicesBusyForNewWrite(StorageModel storage) {
        return isDeviceBusy(writeThreadsPerDeviceMap, storage.getDeviceId(), storage.getMaxWriteThreadsCount());
    }

    private synchronized boolean isDevicesBusyForNewRead(StorageModel storage) {
        return isDeviceBusy(readThreadsPerDeviceMap, storage.getDeviceId(), storage.getMaxReadThreadsCount());
    }


    private boolean isDeviceBusy(Map<Integer, Integer> existsThreadsPerDeviceMap, int deviceId, int maxAllowedThreadCount) {
        Integer existCount = existsThreadsPerDeviceMap.get(deviceId);
        return  (existCount != null && (existCount) >= maxAllowedThreadCount);
    }


    private static class CopyExecution extends BaseExecution {

        private final static int DEFAULT_CHUNK_SIZE = (int) Files.convertFromUnits(32, Files.Units.Kilobyte);

        private FileModel srcFile;
        private FileModel dstFolder;
        private FileModel dstFile;
        private InputStream is;
        private OutputStream os;
        private long fileSizeByteCount = 0;
        private long startTime = 0;
        private long processedByteCount = 0;
        private byte[] buffer = null;
        private long estimationTimeBuffer = 0;


        private CopyExecution(Function<Void, Void> postExecutionAction, TaskModel task, boolean isCleanRun, Logger log) {
            super(postExecutionAction,task, isCleanRun, log);
        }

        @Override
        public void initialize() throws ExecutionUnavailableException {
            srcFile = getTask().getProperty("src", FileModel.class);
            dstFolder = getTask().getProperty("dst", FileModel.class);
            dstFile = dstFolder.createFile(srcFile.getSimpleName());
        }

        @Override
        protected void cleanupPreviousExecution(){
            if (dstFile.isExistsLocally()){
                try {
                    dstFile.remove();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };
        }

        @Override
        protected boolean rollbackAndReleaseResources() throws ExecutionUnavailableException {
            releaseResources();
            cleanupPreviousExecution();
            return true;
        }

        @Override
        protected void allocateResources() throws ExecutionUnavailableException {
            buffer = new byte[DEFAULT_CHUNK_SIZE];
            if (dstFile.isExistsLocally()){
                throw new ExecutionUnavailableException(ExecutionUnavailableException.Reason.execution);
            }
            ExecutionUnavailableException errors = new ExecutionUnavailableException(ExecutionUnavailableException.Reason.execution);
            boolean doThrow = false;
            try {
                os = dstFile.openWriteStream();
            } catch (IOException e) {
                log.warn("[Task ="+getTask().getRef()+"] Error during allocating resources",e);
                doThrow = true;
            }

            try {
                is = srcFile.openReadStream();
            } catch (IOException e) {
                log.warn("[Task ="+getTask().getRef()+"] Error during allocating resources",e);
                doThrow = true;
            }

            if(doThrow) throw errors;
            fileSizeByteCount = srcFile.getByteSize();
        }


        @Override
        protected void releaseResources() throws ExecutionUnavailableException {
            buffer = null;
            ExecutionUnavailableException errors = new ExecutionUnavailableException(ExecutionUnavailableException.Reason.execution);
            boolean doThrow =false;
            try {
                srcFile.closeStream(os);
            } catch (IOException e) {
                log.warn("[Task ="+getTask().getRef()+"] Error during releasing resources",e);
                doThrow = true;
            }
            try {
                dstFile.closeStream(is);
            } catch (IOException e) {
                log.warn("[Task ="+getTask().getRef()+"] Error during releasing resources",e);
                doThrow =true;
            }
            if(doThrow) throw errors;
        }

        @Override
        protected long getApproximatelyStepCount() {
            if (fileSizeByteCount % DEFAULT_CHUNK_SIZE!=0) {
                return fileSizeByteCount / DEFAULT_CHUNK_SIZE + 1;
            } else {
                return fileSizeByteCount / DEFAULT_CHUNK_SIZE;
            }
        }

        @Override
        protected boolean execute(long step, long stepCount) {
            try {
                int readCount = is.read(buffer, 0, buffer.length);
                if (readCount < 0)
                    return true;
                os.write(buffer,0,readCount);
                processedByteCount+=readCount;
                estimationTimeBuffer +=readCount;
                if (step % 2000 == 0){
                    if (startTime != 0) {
                        double timeDelta = (double)(System.currentTimeMillis() - startTime);
                        if(timeDelta == 0) timeDelta = 1000;
                        double speed = (double)estimationTimeBuffer / (double)timeDelta;
                        dstFile.getStorage().setSpeed(speed);
                        long endTime = Math.round((fileSizeByteCount - processedByteCount)/dstFile.getStorage().getSpeed());
                        publishStatistic("end_time", endTime);
                    }
                    startTime = System.currentTimeMillis();
                    estimationTimeBuffer = 0;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return false;
        }

    }

    private static abstract class BaseExecution implements Execution, Runnable {

        private final TaskModel task;
        private final Function<Void,Void> onFinish;
        private Map<String,Object> statisticMap = new HashMap<String, Object>();
        private Float progress = 0f;
        private final boolean cleanRun;
        protected final Logger log;
        private boolean killed = false;

        private BaseExecution(Function<Void, Void> onFinish, TaskModel task, boolean cleanRun, Logger log) {
            this.task = task;
            this.onFinish = onFinish;
            this.cleanRun = cleanRun;
            this.log = log;
        }

        @Override
        public synchronized Float getProgress() {
            return progress;
        }

        private synchronized void setProgress(Float value) {
            progress = value;
        }

        public TaskModel getTask() {
            return task;
        }

        @Override
        public void kill() {
            killed = true;
        }

        @Override
        public void run() {
            log.info("[Task = {}] Execution started ... ", task.getRef());
            try {
                if(!cleanRun){
                    log.info("[Task = {}] Requested cleanup before execution task", task.getRef());
                    cleanupPreviousExecution();
                }
                log.info("[Task = {}] Allocate resources", task.getRef());
                allocateResources();
                long stepCount = getApproximatelyStepCount()+1;
                boolean finished = false; long currentStep=0;
                while(!finished){
                    log.debug("[Task = {}] Executing step {} of {} ...", task.getRef(), currentStep, stepCount);
                    finished = execute(currentStep,stepCount);
                    float currentProgress = 1 / ((float)stepCount) * ((float)currentStep+1);
                    setProgress(currentProgress);
                    if (Thread.currentThread().isInterrupted()){
                        log.warn("[Task = {}] was interrupted ...", task.getRef());
                        throw new InterruptedException();
                    }

                    if (killed){
                        log.warn("[Task = {}] was killed ...", task.getRef());
                        throw new InterruptedException("killed");
                    }

                    if (currentStep>=stepCount){
                        stepCount++;
                        log.warn("[Task = {}] increasing step count ...", task.getRef());
                    }
                    currentStep++;
                }
                log.info("[Task = {}] Execution done. Releasing resources", task.getRef());
                releaseResources();
                task.updateStatus(TaskModel.ExecutionStatus.Finished);
            } catch (Exception e) {
                log.warn("[Task = "+task.getRef()+"] Exception during execution task. ", e);
                try {
                    log.info("[Task = {}] Rollback and release resources", task.getRef());
                    task.updateStatus(killed ? TaskModel.ExecutionStatus.Killed : TaskModel.ExecutionStatus.Fails);
                    if (rollbackAndReleaseResources()){
                        releaseResources();
                    }
                } catch (Exception rollbackException){
                    log.warn("[Task = "+task.getRef()+"] Exception during rollback. ", e);
                }
            }
            onFinish.apply(null);
        }

        protected abstract void cleanupPreviousExecution();

        final public synchronized void publishStatistic(String key,Object value){
            statisticMap.put(key, value);
        }

        @Override
        public synchronized Object getStatistic(String key) {
            return statisticMap.get(key);
        }

        protected abstract void allocateResources() throws ExecutionUnavailableException;

        protected abstract void releaseResources() throws ExecutionUnavailableException;

        protected boolean rollbackAndReleaseResources() throws ExecutionUnavailableException {return false;}

        protected abstract long getApproximatelyStepCount();

        protected abstract boolean execute(long step, long stepCount);

        public abstract void initialize() throws ExecutionUnavailableException;
    }

}
