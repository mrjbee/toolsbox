package org.monroe.team.toolsbox.services.impl;

import com.google.common.base.Function;
import org.monroe.team.toolsbox.entities.Execution;
import org.monroe.team.toolsbox.logging.Logs;
import org.monroe.team.toolsbox.services.ExecutionManager;
import org.monroe.team.toolsbox.us.model.FileModel;
import org.monroe.team.toolsbox.us.model.StorageModel;
import org.monroe.team.toolsbox.us.model.TaskModel;

import javax.annotation.PreDestroy;
import javax.inject.Named;
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

    @PreDestroy
    public void shutdown(){
        Logs.core.info("Execution manager graceful shutdown started.");
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logs.core.warn("Execution manager graceful shutdown was interrupted.", e);
        }
    }

    @Override
    public Execution getTaskExecution(Integer taskId) {
        return currentExecutionsMap.get(taskId);
    }

    @Override
    public synchronized void executeAsCopyTask(final TaskModel taskModel, boolean restart) throws ExecutionUnavailableException {
        final FileModel srcFile = taskModel.getProperty("src", FileModel.class);
        final FileModel dstFile = taskModel.getProperty("dst", FileModel.class);
        if (!srcFile.isExistsLocally() || !dstFile.isExistsLocally()) {
            throw new ExecutionUnavailableException(ExecutionUnavailableException.Reason.no_file);
        }
        if (isDevicesBusyForNewRead(srcFile.getStorage()) && isDevicesBusyForNewWrite(dstFile.getStorage())){
            throw new ExecutionUnavailableException(ExecutionUnavailableException.Reason.device_is_busy);
        }

        final int readDeviceId = srcFile.getStorage().getDeviceId();
        final int writeDeviceId = dstFile.getStorage().getDeviceId();

        final BaseExecution execution = new CopyExecution(new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                freeThreads(readDeviceId, writeDeviceId);
                currentExecutionsMap.remove(taskModel.getRef());
                return null;
            }
        },taskModel, !restart);
        captureThreads(readDeviceId, writeDeviceId);
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
        return  !(existCount == null || (existCount+1) <= maxAllowedThreadCount);
    }


    private static class CopyExecution extends BaseExecution {

        private CopyExecution(Function<Void, Void> postExecutionAction, TaskModel task, boolean isCleanRun) {
            super(postExecutionAction,task, isCleanRun);
        }

        @Override
        protected void cleanupPreviousExecution() {

        }

        @Override
        protected void allocateResources() throws ExecutionUnavailableException {

        }

        @Override
        protected void releaseResources() {

        }

        @Override
        protected int getStepCount() {
            return 10;
        }

        @Override
        protected void execute(int i, int stepCount) {
            try {
                if (i == 5 && getTask().getRef()%3 == 0){
                    throw new IllegalStateException("Something wrong");
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                //nothing here
            }
        }

    }

    private static abstract class BaseExecution implements Execution, Runnable {

        private final TaskModel task;
        private final Function<Void,Void> onFinish;
        private Map<String,String> statisticMap = new HashMap<String, String>();
        private Float progress = 0f;
        private final boolean cleanRun;

        private BaseExecution(Function<Void, Void> onFinish, TaskModel task, boolean cleanRun) {
            this.task = task;
            this.onFinish = onFinish;
            this.cleanRun = cleanRun;
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
        public void run() {
            try {
                if(!cleanRun){
                    cleanupPreviousExecution();
                }
                allocateResources();
                int stepCount = getStepCount();
                for (int i=0;i<stepCount;i++){
                    execute(i,stepCount);
                    float currentProgress = ((float)stepCount) / 100 * ((float)i+1);
                    setProgress(currentProgress);
                    if (Thread.currentThread().isInterrupted()){
                        task.updateStatus(TaskModel.ExecutionStatus.Fails);
                        break;
                    }
                }
                releaseResources();
                task.updateStatus(TaskModel.ExecutionStatus.Finished);
            } catch (Exception e) {
                Logs.core.warn("Exception during execution task = "+task.getRef(), e);
                try {
                    if (rollbackAndReleaseResources()){
                        releaseResources();
                    }
                    task.updateStatus(TaskModel.ExecutionStatus.Fails);
                } catch (Exception rollbackException){
                    Logs.core.warn("Exception during rollback task = "+task.getRef(), rollbackException);
                }
            }
            onFinish.apply(null);
        }

        protected abstract void cleanupPreviousExecution();

        final public synchronized void publishStatistic(String key,String value){
            statisticMap.put(key, value);
        }

        @Override
        public synchronized String getStatistic(String key) {
            return statisticMap.get(key);
        }

        protected abstract void allocateResources() throws ExecutionUnavailableException;

        protected abstract void releaseResources();

        protected boolean rollbackAndReleaseResources(){return false;};

        protected abstract int getStepCount();

        protected abstract void execute(int i, int stepCount);

    }

}
