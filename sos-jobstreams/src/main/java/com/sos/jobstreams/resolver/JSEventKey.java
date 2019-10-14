package com.sos.jobstreams.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSEventKey {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSEventKey.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private String session;
    private String event;
    private String jobStream;
    private String schedulerId;
    private Boolean globalEvent = false;

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof JSEventKey) {
            JSEventKey jsEventKey = (JSEventKey) obj;
            if (jsEventKey.globalEvent == null) {
                jsEventKey.setGlobalEvent(false);
                if (isDebugEnabled) {
                    LOGGER.debug("!!!!!!! NPE in JSEventKey");
                }
            }
            return this.globalEvent.equals(jsEventKey.globalEvent) && session.equals(jsEventKey.session) && event.equals(jsEventKey.event)
                    && jobStream.equals(jsEventKey.jobStream) && schedulerId.equals(jsEventKey.schedulerId);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (globalEvent + "." + session + "." + event + "." + jobStream + "." + schedulerId).hashCode();
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

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public Boolean getGlobalEvent() {
        return globalEvent;
    }

    public void setGlobalEvent(Boolean globalEvent) {
        this.globalEvent = globalEvent;
    }

}