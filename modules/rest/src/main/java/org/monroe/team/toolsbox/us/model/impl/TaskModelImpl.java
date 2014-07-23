package org.monroe.team.toolsbox.us.model.impl;

import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.entities.Execution;
import org.monroe.team.toolsbox.entities.Property;
import org.monroe.team.toolsbox.entities.Task;
import org.monroe.team.toolsbox.repositories.PropertyRepository;
import org.monroe.team.toolsbox.services.ExecutionManager;
import org.monroe.team.toolsbox.services.FileManager;
import org.monroe.team.toolsbox.services.Files;
import org.monroe.team.toolsbox.services.TaskManager;
import org.monroe.team.toolsbox.us.model.FileModel;
import org.monroe.team.toolsbox.us.model.TaskModel;
import org.monroe.team.toolsbox.us.model.impl.dependecy.Dependency;

import java.text.DecimalFormat;

public class TaskModelImpl implements TaskModel {

    private final Dependency<Task> taskDependency;
    private final Dependency<Execution> executionDependency;
    private final TaskManager taskManager;
    private final PropertyRepository propertyRepository;
    private final ExecutionManager executionManager;
    private final FileManager fileManager;

    public TaskModelImpl(Dependency<Task> taskDependency,
                         Dependency<Execution> executionDependency, TaskManager taskManager,
                         PropertyRepository propertyRepository,
                         ExecutionManager executionManager,
                         FileManager fileManager) {
        this.taskDependency = taskDependency;
        this.executionDependency = executionDependency;
        this.taskManager = taskManager;
        this.propertyRepository = propertyRepository;
        this.executionManager = executionManager;
        this.fileManager = fileManager;
    }

    @Override
    public org.monroe.team.toolsbox.us.model.TaskModel withProperty(String src, String value) {
        check(isHealthy());
        if (taskDependency.get().properties == null)
            taskDependency.get().properties = Lists.newArrayList();
        taskDependency.get().properties.add(propertyRepository.save(new Property(src, value)));
        taskDependency.save();
        return this;
    }

    @Override
    public Integer getRef() {
        check(isHealthy());
        return taskDependency.get().id;
    }

    @Override
    public ExecutionStatus getStatus() {
        check(isHealthy());
        return taskDependency.get().status;
    }

    @Override
    public String getStatusAsString() {
        return getStatus().name();
    }

    @Override
    public Type getType() {
        check(isHealthy());
        return taskDependency.get().type;
    }

    @Override
    public String getTypeAsString() {
        return getType().name();
    }

    @Override
    public Float getExecutionProgress() {
        switch (getStatus()){
            case Pending: return 0f;
            case Restoring: return 0f;
            case Finished: return 1f;
            case Fails: return 0.5f;
            case Progress:
                if (!executionDependency.exists()) return 0f;
                return executionDependency.get().getProgress();
        }
        return 0f;
    }

    @Override
    public <Type> Type getProperty(String key, Class<Type> requestedType) {
        check(isHealthy());
        if (FileModel.class.equals(requestedType)){
            Integer fileId = taskDependency.get().getProperty(key,Integer.class);
            return (Type) fileManager.getById(fileId);
        }
        return taskDependency.get().getProperty(key, requestedType);
    }


    @Override
    public synchronized  boolean execute() throws ExecutionManager.ExecutionPendingException {
        return executeImpl(false);
    }


    @Override
    public synchronized boolean restart() throws ExecutionManager.ExecutionPendingException {
        return executeImpl(true);
    }

    @Override
    public synchronized boolean destroy() {
        if (ExecutionStatus.Progress.equals(getStatus())){
            return false;
        }
        taskDependency.delete();
        return true;
    }

    @Override
    public synchronized boolean stop() {
        if (!executionDependency.exists()){
            return false;
        }
        executionDependency.get().kill();
        return true;
    }

    private boolean executeImpl(boolean restart) throws ExecutionManager.ExecutionPendingException {
        if (taskDependency.refresh() == null) return false;
        switch (getType()){
            case COPY:
                executionManager.executeAsCopyTask(this,restart);
                break;
            case DOWNLOAD:
                executionManager.executeAsDownloadTask(this,restart);
                break;
            default:
                throw new UnsupportedOperationException("Could not execute:"+getType());
        }
        return true;
    }

    @Override
    public void updateStatus(ExecutionStatus status) {
        check(isHealthy());
        taskDependency.get().status = status;
        if (!ExecutionStatus.isExecutionAwaiting(status)){
            taskDependency.get().pendingReason = null;
        }
        taskDependency.save();
    }

    @Override
    public boolean isHardInterrupted() {
        return getStatus().equals(ExecutionStatus.Progress) && !executionDependency.exists();
    }

    @Override
    public String getExecutionSpeed() {
        switch (getType()){
            case COPY:
                FileModel dst = getProperty("dst", FileModel.class);
                if (!dst.isExistsLocally()) return "NaN";
                double speed = dst.getStorage().getSpeed();
                return convertSpeedToHuman(speed);
            case DOWNLOAD:
                if (executionDependency.exists()){
                    Double downloadSpeed = (Double) executionDependency.get().getStatistic("speed");
                    if (downloadSpeed!= null) return convertSpeedToHuman(downloadSpeed);
                }
                return "NaN";
            default: return "NaN";
        }


    }

    @Override
    public void setPendingReason(String reason) {
         check(isHealthy());
         taskDependency.get().pendingReason=reason;
         taskDependency.save();
    }

    @Override
    public String getPendingReason() {
        return taskDependency.get().pendingReason;
    }

    private String convertSpeedToHuman(double speed) {
        if (speed == 0) return "0 mb/sec";
        double bytesInMs = speed;
        double mByteInMin = bytesInMs * 1000 / Files.convertFromUnits(1, Files.Units.Megabyte);
        return new DecimalFormat("##.##").format(mByteInMin)+" mb/sec";
    }

    @Override
    public String getEstimationDateString() {

        if (executionDependency.exists()){
            long msCount = 0;
            Long ms = (Long) executionDependency.get().getStatistic("end_time");
            if (ms != null){
                msCount = ms;
            }
            return msToHuman(msCount);
        }

        switch (getType()){
            case COPY:
                    try {
                        long msCount = 0;
                        double speed = getProperty("dst", FileModel.class).getStorage().getSpeed();
                        long size = getProperty("src", FileModel.class).getByteSize();
                        if (speed == 0) return "NaN";
                        msCount = Math.round(size / speed);
                        return msToHuman(msCount);
                    }catch (Exception e){
                        return "NaN";
                    }
            case DOWNLOAD:
                    return "NaN";
            default:
                    return "NaN";
        }
    }

    private String msToHuman(long msCount) {
        StringBuffer result = new StringBuffer();
        long hr =0, min =0, sec =0;
        if (msCount/(1000*60*60) != 0){
            hr = msCount/(1000*60*60);
            msCount = msCount%(1000*60*60);
        }
        if (msCount/(1000*60) != 0){
            min = msCount/(1000*60);
            msCount = msCount%(1000*60);
        }
        if (msCount/(1000) != 0){
            sec = msCount/(1000);
        }
        if (hr != 0) result.append(hr+" hr ");
        result.append(min+" min ");
        result.append(sec+" sec");
        return result.toString();
    }

    private void check(boolean condition) {
        if (!condition){
            throw new IllegalStateException();
        }
    }

    private boolean isHealthy() {
        return taskDependency.exists();
    }

}
