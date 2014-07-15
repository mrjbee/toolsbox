package org.monroe.team.toolsbox.us;

public interface StopTaskDefinition {

    public void perform(Integer taskId) throws TaskNotFoundException, TaskStopNotAllowedException;

    public static  class TaskNotFoundException extends Exception{}
    public static  class TaskStopNotAllowedException extends Exception{}
}
