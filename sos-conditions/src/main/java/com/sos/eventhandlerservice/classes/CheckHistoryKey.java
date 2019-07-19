package com.sos.eventhandlerservice.classes;

import com.sos.eventhandlerservice.resolver.JSEventKey;

public class CheckHistoryKey {
    public CheckHistoryKey(String job, String query) {
        super();
        this.job = job;
        this.query = query;
    }

    private String job;
    private String query;
    
    public String getJob() {
        return job;
    }
    
    public void setJob(String job) {
        this.job = job;
    }
    
    public String getQuery() {
        return query.toLowerCase();
    }
    
    public void setQuery(String query) {
        this.query = query.toLowerCase();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof CheckHistoryKey) {
            CheckHistoryKey checkHistoryKey = (CheckHistoryKey) obj;
            return job.equals(checkHistoryKey.job) && query.equals(checkHistoryKey.query);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (job + "." + query).hashCode();
    }
}
