package com.sos.jobstreams.classes;

import com.sos.jobstreams.resolver.JSJobStreamStarter;

public class StarterScheduleTableItem {
    private JSJobStreamStarter starter;
    private boolean started;
    private long startTime=0L;
    
    public JSJobStreamStarter getStarter() {
        return starter;
    }
    
    public void setStarter(JSJobStreamStarter starter) {
        this.starter = starter;
    }
    
    public boolean isStarted() {
        return started;
    }
    
    public void setStarted(boolean started) {
        this.started = started;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

}
