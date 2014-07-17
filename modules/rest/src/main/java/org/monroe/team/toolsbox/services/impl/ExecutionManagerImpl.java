package org.monroe.team.toolsbox.services.impl;

import com.google.common.base.Function;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.entities.Execution;
import org.monroe.team.toolsbox.services.ExecutionManager;
import org.monroe.team.toolsbox.services.Files;
import org.monroe.team.toolsbox.transport.AverageCalculator;
import org.monroe.team.toolsbox.us.model.FileModel;
import org.monroe.team.toolsbox.us.model.StorageModel;
import org.monroe.team.toolsbox.us.model.TaskModel;
import org.monroe.team.toolsbox.us.model.impl.TaskModelImpl;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.inject.Named;
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
    public synchronized void executeAsCopyTask(final TaskModel taskModel, boolean restart) throws ExecutionPendingException {
        final FileModel srcFile = taskModel.getProperty("src", FileModel.class);
        final FileModel dstFile = taskModel.getProperty("dst", FileModel.class);
        if (!srcFile.isExistsLocally() || !dstFile.isExistsLocally()) {
            throw new ExecutionPendingException(ExecutionPendingException.Reason.no_file);
        }
        if (isDevicesBusyForNewRead(srcFile.getStorage()) || isDevicesBusyForNewWrite(dstFile.getStorage())){
            throw new ExecutionPendingException(ExecutionPendingException.Reason.device_is_busy);
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

    @Override
    public void executeAsDownloadTask(final TaskModelImpl taskModel, boolean restart) throws ExecutionPendingException {
        final FileModel dstFolder = taskModel.getProperty("dst", FileModel.class);
        if (!dstFolder.isExistsLocally()) {
            throw new ExecutionPendingException(ExecutionPendingException.Reason.no_file);
        }
        final BaseExecution execution = new DownloadExecution(new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                taskLog.info("Free resources for task = {}", taskModel.getRef());
                currentExecutionsMap.remove(taskModel.getRef());
                return null;
            }
        },taskModel, !restart, taskLog);
        taskLog.info("Capture resources for task = {}", taskModel.getRef());
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


    private static class DownloadExecution extends StreamCopyExecution{

        private FileModel dstFolder;
        private FileModel dstFile;
        private String url;
        private CloseableHttpClient httpclient;
        private long byteLen = 0;

        protected DownloadExecution(Function<Void, Void> postExecutionAction, TaskModel task, boolean isCleanRun, Logger log) {
            super(postExecutionAction, task, isCleanRun, log);
        }

        @Override
        protected void publishCopyStatistic(double speed, long endTime) {
            publishStatistic("speed",speed);
            publishStatistic("end_time",endTime);
        }

        @Override
        protected OutputStream getWriteStream() throws IOException {
            return dstFile.openWriteStream();
        }

        @Override
        protected InputStream getReadStream() throws IOException {
            httpclient = HttpClientBuilder.create()
                    .setUserAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0").build();
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Content-Type", "charset=UTF-8");
            HttpResponse response = null;
            try {
                response = httpclient.execute(httpget);
            } catch (Exception e) {
                log.warn("[Task ="+getTask().getRef()+"] Error during getting HTTP stream",e);
                throw new RuntimeException(e);
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) throw new RuntimeException("No body");
            byteLen = response.getEntity().getContentLength();
            if (byteLen<0) byteLen=1;
            return entity.getContent();
        }


        @Override
        protected void releaseResources() {
            super.releaseResources();
            //Close HTTP Connection
            if (httpclient != null){
                try {
                    httpclient.close();
                    httpclient = null;
                } catch (IOException e) {
                    log.warn("[Task ="+getTask().getRef()+"] Error during releasing HTTP resources",e);
                    httpclient = null;
                    throw new RuntimeException("Exception during closing HTTP client",e);
                }
            }
        }

        @Override
        protected long getCopyByteCount() {
            return byteLen;
        }

        @Override
        protected void cleanupPreviousExecution() {
            if(dstFile.isExistsLocally()){
                try {
                    dstFile.remove();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void initialize() throws ExecutionPendingException {
            dstFolder = getTask().getProperty("dst", FileModel.class);
            dstFile = dstFolder.createFile(getTask().getProperty("fileName", String.class));
            url = getTask().getProperty("url",String.class);
        }
    }

    private static class CopyExecution extends StreamCopyExecution{

        private FileModel srcFile;
        private FileModel dstFolder;
        private FileModel dstFile;
        //TODO: after system restart copy wouldn`t happend
        private boolean fileExistsBeforeCopy;

        protected CopyExecution(Function<Void, Void> postExecutionAction, TaskModel task, boolean isCleanRun, Logger log) {
            super(postExecutionAction, task, isCleanRun, log);
        }

        @Override
        public void initialize() throws ExecutionPendingException {
            srcFile = getTask().getProperty("src", FileModel.class);
            dstFolder = getTask().getProperty("dst", FileModel.class);
            dstFile = dstFolder.createFile(srcFile.getSimpleName());
        }


        @Override
        protected void cleanupPreviousExecution() {
            if (dstFile.isExistsLocally() && !fileExistsBeforeCopy){
                try {
                    dstFile.remove();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        protected void publishCopyStatistic(double speed, long endTime) {
            dstFile.getStorage().setSpeed(speed);
            publishStatistic("speed",speed);
            publishStatistic("end_time",endTime);
        }

        @Override
        protected OutputStream getWriteStream() throws IOException {
            if (dstFile.isExistsLocally()){
                fileExistsBeforeCopy = true;
                throw new IOException("File exists");
            }
            return dstFile.openWriteStream();
        }

        @Override
        protected InputStream getReadStream() throws IOException {
            return srcFile.openReadStream();
        }

        @Override
        protected long getCopyByteCount() {
            return srcFile.getByteSize();
        }

    }

    private static abstract class StreamCopyExecution extends BaseExecution {

        private final static int DEFAULT_CHUNK_SIZE = (int) Files.convertFromUnits(32, Files.Units.Kilobyte);

        private InputStream is;
        private OutputStream os;
        private long totalByteCount = 0;
        private long startTime = 0;
        private long readedByteCount = 0;
        private byte[] buffer = null;
        private long estimationTimeBuffer = 0;
        private AverageCalculator speedCalc = new AverageCalculator(12);

        protected StreamCopyExecution(Function<Void, Void> postExecutionAction, TaskModel task, boolean isCleanRun, Logger log) {
            super(postExecutionAction,task, isCleanRun, log);
        }

        @Override
        protected void allocateResources() {
            buffer = new byte[DEFAULT_CHUNK_SIZE];
            try {
                os = getWriteStream();
            } catch (IOException e) {
                log.warn("[Task =" + getTask().getRef() + "] Error during allocating resources", e);
                throw new RuntimeException(e);
            }

            try {
                is = getReadStream();
            } catch (IOException e) {
                log.warn("[Task =" + getTask().getRef() + "] Error during allocating resources", e);
                throw new RuntimeException(e);
            }

            totalByteCount = getCopyByteCount();
        }


        @Override
        protected void releaseResources()  {
            buffer = null;
            RuntimeException ex = null;

            try {
                if (os != null) os.close();
            } catch (IOException e) {
                log.warn("[Task ="+getTask().getRef()+"] Error during releasing resources",e);
                ex = new RuntimeException(e);
            }

            try {
                if(is != null) is.close();
            } catch (IOException e) {
                log.warn("[Task ="+getTask().getRef()+"] Error during releasing resources",e);
                ex = new RuntimeException(e);
            }

            if(ex != null) throw ex;
        }

        @Override
        protected boolean rollbackAndReleaseResources() {
            releaseResources();
            cleanupPreviousExecution();
            return true;
        }


        private int step = 0;
        @Override
        protected boolean execute() {
            int readCount = -1;
            try {
                readCount = is.read(buffer, 0, buffer.length);
                if (readCount <= 0)
                    return true;
                os.write(buffer,0,readCount);
                readedByteCount += readCount;
                estimationTimeBuffer += readCount;
                if (step % 1000 == 0){
                    if (startTime != 0) {
                        double timeDelta = (double)(System.currentTimeMillis() - startTime);
                        if(timeDelta == 0) timeDelta = 1000;
                        double speed = (double)estimationTimeBuffer / (double)timeDelta;
                        speedCalc.put(speed);
                    }
                    startTime = System.currentTimeMillis();
                    estimationTimeBuffer = 0;
                }
                long endTime = Math.round((totalByteCount - readedByteCount)/speedCalc.get());
                publishCopyStatistic(speedCalc.get(), endTime);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            step++;
            return false;
        }

        @Override
        final protected Float calculateCurrentProgress() {
            //Unknown how much bytes to read
            if (totalByteCount < 1) return 0.5f;
            float progress = (1/(float)totalByteCount * (float)readedByteCount);
            //If read more then expected
            if (progress>1f){
                progress = 0.9f;
            }
            return progress;
        }

        protected abstract void publishCopyStatistic(double speed, long endTime);
        protected abstract OutputStream getWriteStream() throws IOException;
        protected abstract InputStream getReadStream() throws IOException;
        protected abstract long getCopyByteCount();
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
                boolean finished = false;
                while(!finished){
                    finished = execute();
                    log.debug("[Task = {}] Executing progress {} ...", task.getRef(), getProgress());
                    setProgress(calculateCurrentProgress());
                    if (Thread.currentThread().isInterrupted()){
                        log.warn("[Task = {}] was interrupted ...", task.getRef());
                        throw new InterruptedException();
                    }

                    if (killed){
                        log.warn("[Task = {}] was killed ...", task.getRef());
                        throw new InterruptedException("killed");
                    }
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

        protected abstract Float calculateCurrentProgress();

        protected abstract void cleanupPreviousExecution();

        final public synchronized void publishStatistic(String key,Object value){
            statisticMap.put(key, value);
        }

        @Override
        public synchronized Object getStatistic(String key) {
            return statisticMap.get(key);
        }

        protected abstract void allocateResources();

        protected abstract void releaseResources();

        protected boolean rollbackAndReleaseResources() {return false;}

        protected abstract boolean execute();

        public abstract void initialize() throws ExecutionPendingException;
    }

}
