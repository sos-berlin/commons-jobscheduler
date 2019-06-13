package com.sos.eventhandlerservice.resolver;

public class JSEventKey {

    private String session;
    private String event;
    private String workflow;

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof JSEventKey) {
            JSEventKey jsEventKey = (JSEventKey) obj;
            return session.equals(jsEventKey.session) && event.equals(jsEventKey.event) && workflow.equals(jsEventKey.workflow);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (session + "." + event + "." + workflow).hashCode();
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

    
    public String getWorkflow() {
        return workflow;
    }

    
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

}