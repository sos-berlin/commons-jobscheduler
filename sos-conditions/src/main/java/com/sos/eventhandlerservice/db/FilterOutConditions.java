package com.sos.eventhandlerservice.db;

import java.util.ArrayList;
import java.util.List;

public class FilterOutConditions {

    private String jobSchedulerId;
    private String job;
    private String workflow;
    private List<String> events;

    public String getJobSchedulerId() {
        return jobSchedulerId;
    }

    public void setJobSchedulerId(String jobSchedulerId) {
        this.jobSchedulerId = jobSchedulerId;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public void addEvent(String event) {
        if (events == null) {
            events = new ArrayList<String>();
        }
        if (!events.contains(event)) {
            events.add(event);
        }
    }

    public List<String> getListOfEvents() {
        return events;
    }
}
