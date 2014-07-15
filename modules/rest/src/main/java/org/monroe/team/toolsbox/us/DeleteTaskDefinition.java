package org.monroe.team.toolsbox.us;

public interface DeleteTaskDefinition {
    public void perform(Integer taskId) throws TaskNotFoundException, TaskCleaningNotAllowedException;

    public static  class TaskNotFoundException extends Exception{}
    public static  class TaskCleaningNotAllowedException extends Exception{}
}
