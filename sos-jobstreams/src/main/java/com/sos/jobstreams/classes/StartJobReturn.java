package com.sos.jobstreams.classes;

public class StartJobReturn {

    private String startedJob;
    private Long taskId;
    private boolean markedAsConsumed=false;

    public String getStartedJob() {
        return startedJob;
    }

    public void setStartedJob(String startedJob) {
        this.startedJob = startedJob;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    
    public boolean isMarkedAsConsumed() {
        return markedAsConsumed;
    }

    
    public void setMarkedAsConsumed(boolean markedAsConsumed) {
        this.markedAsConsumed = markedAsConsumed;
    }

}
