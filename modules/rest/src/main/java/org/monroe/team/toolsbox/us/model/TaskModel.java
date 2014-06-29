package org.monroe.team.toolsbox.us.model;


import org.monroe.team.toolsbox.entities.Task;

public interface TaskModel {
    TaskModel withProperty(String src, String value);
    Integer getRef();

    Task.ExecutionStatus getStatus();

    String getStatusAsString();

    Task.Type getType();

    String getTypeAsString();

    Float getProgress();

    <Type> Type getProperty(String src, Class<Type> requestedType);
}
