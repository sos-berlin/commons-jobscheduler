package com.sos.jobstreams.classes;


public class StartedItem {
    private long starterId;
    private long startTime;
    
    public long getStarterId() {
        return starterId;
    }
    
    public void setStarterId(long starterId) {
        this.starterId = starterId;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (startTime ^ (startTime >>> 32));
        result = prime * result + (int) (starterId ^ (starterId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StartedItem other = (StartedItem) obj;
        if (startTime != other.startTime)
            return false;
        if (starterId != other.starterId)
            return false;
        return true;
    }

}
