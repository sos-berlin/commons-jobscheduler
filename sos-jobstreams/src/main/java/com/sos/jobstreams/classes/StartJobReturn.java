package com.sos.jobstreams.classes;

public class StartJobReturn {

    private String startedJob;
    private Long taskId;
    private boolean markedAsConsumed=false;
    private String errCode;
    private String errText;
    private boolean started;

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

    
    public String getErrCode() {
        return errCode;
    }

    
    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    
    public String getErrText() {
        return errText;
    }

    
    public void setErrText(String errText) {
        this.errText = errText;
    }

    
    public boolean isStarted() {
        return started;
    }

    
    public void setStarted(boolean started) {
        this.started = started;
    }

}
