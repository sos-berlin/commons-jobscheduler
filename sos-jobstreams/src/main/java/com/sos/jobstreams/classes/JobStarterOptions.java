package com.sos.jobstreams.classes;

import java.util.Map;

public class JobStarterOptions {
    private String normalizedJob;
    private String jobStream;
    private String job;
    private Long taskId;
    private Map<String,String>listOfParameters;
    
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

    
    public Map<String, String> getListOfParameters() {
        return listOfParameters;
    }

    
    public void setListOfParameters(Map<String, String> listOfParameters) {
        this.listOfParameters = listOfParameters;
    }
}
