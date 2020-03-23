package com.sos.jobstreams.classes;


public class StartJobReturn {
    
    private String startedJob;
    private Long taskId;
    
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

}
