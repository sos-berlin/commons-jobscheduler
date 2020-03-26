package com.sos.jobstreams.classes;


public class JobStarterOptions {
    private String normalizedJob;
    private String jobStream;
    private String job;
    private Long taskId;
    
    public String getNormalizedJob() {
        return normalizedJob;
    }
    
    public void setNormalizedJob(String normalizedJob) {
        this.normalizedJob = normalizedJob;
    }
    
    public String getJobStream() {
        return jobStream;
    }
    
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }
    
    public String getJob() {
        return job;
    }
    
    public void setJob(String job) {
        this.job = job;
    }

    
    public Long getTaskId() {
        return taskId;
    }

    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
