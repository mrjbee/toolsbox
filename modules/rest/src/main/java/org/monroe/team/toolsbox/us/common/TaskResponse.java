package org.monroe.team.toolsbox.us.common;

import java.util.HashMap;
import java.util.Map;

public class TaskResponse {

    public final Integer taskId;
    public final String status;
    public final String type;
    public final String endDate;
    public final Float progress;
    public final String awaitingReason;
    public final Map<String, String> details;


    public TaskResponse(Integer taskId, String status, String type, String endDate, Float progress, String awaitingReason) {
        this.taskId = taskId;
        this.status = status;
        this.type = type;
        this.endDate = endDate;
        this.progress = progress;
        this.awaitingReason = awaitingReason;
        this.details = new HashMap<String, String>();
    }

    public TaskResponse with(String detailName, String detailValue){
        details.put(detailName, detailValue);
        return this;
    }
}
