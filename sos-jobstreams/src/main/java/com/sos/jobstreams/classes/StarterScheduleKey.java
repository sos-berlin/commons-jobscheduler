package com.sos.jobstreams.classes;

public class StarterScheduleKey {

    public String starterName;
    public long scheduledFor=0L;
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (scheduledFor ^ (scheduledFor >>> 32));
        result = prime * result + ((starterName == null) ? 0 : starterName.hashCode());
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
        StarterScheduleKey other = (StarterScheduleKey) obj;
        if (scheduledFor != other.scheduledFor)
            return false;
        if (starterName == null) {
            if (other.starterName != null)
                return false;
        } else if (!starterName.equals(other.starterName))
            return false;
        return true;
    }

 

    
}
