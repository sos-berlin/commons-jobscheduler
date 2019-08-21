package com.sos.jobstreams.resolver;

public class JSEventKey {

    private String session;
    private String event;
    private String jobStream;

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof JSEventKey) {
            JSEventKey jsEventKey = (JSEventKey) obj;
            return session.equals(jsEventKey.session) && event.equals(jsEventKey.event) && jobStream.equals(jsEventKey.jobStream);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (session + "." + event + "." + jobStream).hashCode();
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    
    public String getJobStream() {
        return jobStream;
    }

    
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

}