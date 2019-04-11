package com.sos.eventhandlerservice.resolver;

public class JSEventKey {

    private String session;
    private String event;

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof JSEventKey) {
            JSEventKey jsEventKey = (JSEventKey) obj;
            return session.equals(jsEventKey.session) && event.equals(jsEventKey.event);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (session + "." + event).hashCode();
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

}