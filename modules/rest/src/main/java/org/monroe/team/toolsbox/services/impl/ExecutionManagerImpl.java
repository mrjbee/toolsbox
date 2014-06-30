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
    private Map <Integer, Execution> currentExecution = new HashMap<Integer, Execution>();
    private Map <Integer, Integer> readThreadsPerDeviceMap = new HashMap<Integer, Integer>();
    private Map <Integer, Integer> writeThreadsPerDeviceMap = new HashMap<Integer, Integer>();

    @PreDestroy
    public void shutdown(){
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logs.core.warn("Execution manager graceful shutdown was interrupted.", e);
        }
    }

    @Override
    public Execution getTaskExecution(Integer taskId) {
        return currentExecution.get(taskId);
    }

    @Override
    public synchronized void executeAsCopyTask(TaskModel taskModel) throws ExecutionUnavailableException {
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

        BaseExecution execution = new CopyExecution(new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                freeThreads(readDeviceId, writeDeviceId);
                return null;
            }
        },taskModel);
        captureThreads(readDeviceId, writeDeviceId);
        taskModel.updateStatus(TaskModel.ExecutionStatus.Progress);
        executorService.execute(execution);
    }

    private synchronized void freeThreads(int readDeviceId, int writeDeviceId) {
        captureThread(readDeviceId, readThreadsPerDeviceMap,-1);
        captureThread(writeDeviceId, writeThreadsPerDeviceMap,-1);
    }

    private synchronized void captureThreads(int readFromStorage, int writeToStorage) {
        captureThread(readFromStorage, readThreadsPerDeviceMap,1);
        captureThread(writeToStorage, writeThreadsPerDeviceMap,1);
    }

    private void captureThread(int deviceId, Map<Integer, Integer> threadPerDeviceMap, int delta) {
        Integer existsCount = threadPerDeviceMap.get(deviceId);
        if (existsCount == null) existsCount = new Integer(0);
        threadPerDeviceMap.put(deviceId, existsCount+delta);
    }


    private synchronized boolean isDevicesBusyForNewWrite(StorageModel storage) {
        return isDeviceBusy(writeThreadsPerDeviceMap, storage.getIdentifier(), storage.getMaxWriteThreadsCount());
    }

    private synchronized boolean isDevicesBusyForNewRead(StorageModel storage) {
        return isDeviceBusy(readThreadsPerDeviceMap, storage.getIdentifier(), storage.getMaxReadThreadsCount());
    }


    private boolean isDeviceBusy(Map<Integer, Integer> existsThreadsPerDeviceMap, int deviceId, int maxAllowedThreadCount) {
        Integer existCount = existsThreadsPerDeviceMap.get(deviceId);
        return  (existCount == null || (existCount+1) <= maxAllowedThreadCount);
    }


    private static class CopyExecution extends BaseExecution {

        private CopyExecution(Function<Void, Void> postExecutionAction, TaskModel task) {
            super(postExecutionAction,task);
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
                Thread.sleep(1000);
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

        private BaseExecution(Function<Void,Void> onFinish, TaskModel task) {
            this.task = task;
            this.onFinish = onFinish;
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
        }

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
