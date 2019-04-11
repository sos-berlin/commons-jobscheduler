package com.sos.eventhandlerservice.classes;

import javax.json.JsonObject;

public class TaskEndEvent {
    // returnCode":0,"TYPE":"TaskEnded","key":{"jobPath":"/job 5","taskId":"111070"},"eventId":1554727756541000}

    private Integer returnCode;
    private String type;
    private String jobPath;
    private String taskId;
    private Integer eventId;

    public TaskEndEvent(JsonObject entry) {
        super();
        this.returnCode = entry.getInt("returnCode");
        this.type = entry.getString("TYPE");

        JsonObject key = entry.getJsonObject("key");
        if (key != null) {
            this.jobPath = key.getString("jobPath");
            this.taskId = key.getString("taskId");
        }
        this.eventId = entry.getInt("eventId");
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public String getType() {
        return type;
    }

    public String getJobPath() {
        return jobPath;
    }

    public String getTaskId() {
        return taskId;
    }

    public Integer getEventId() {
        return eventId;
    }

}
