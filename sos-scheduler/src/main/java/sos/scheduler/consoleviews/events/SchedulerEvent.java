package sos.scheduler.consoleviews.events;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class SchedulerEvent {

    protected String eventTitle;
    protected String eventClass;
    protected String eventId;
    protected String jobName;
    protected String jobChain;
    protected String orderId;
    protected String exitCode;
    protected String created;
    protected String expires;
    protected String remoteSchedulerHost;
    protected String remoteSchedulerPort;
    protected String schedulerId;
    protected String logic = "";
    protected String comment = "";
    private String eventName;

    public String getEventTitle() {
        return eventTitle;
    }

    public String getEventClass() {
        return eventClass;
    }

    public String getEventId() {
        return eventId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getJobChain() {
        return jobChain;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getExitCode() {
        return exitCode;
    }

    public String getCreated() {
        return created;
    }

    public String getExpires() {
        return expires;
    }

    public String getRemoteSchedulerHost() {
        return remoteSchedulerHost;
    }

    public String getRemoteSchedulerPort() {
        return remoteSchedulerPort;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public String getLogic() {
        return logic;
    }

    private HashMap<String,String> properties() {
        HashMap <String,String> attr = new HashMap<String,String>();
        attr.put("event_title", eventTitle);
        attr.put("event_class", eventClass);
        attr.put("event_id", eventId);
        attr.put("job_name", jobName);
        attr.put("job_chain", jobChain);
        attr.put("order_id", orderId);
        attr.put("exit_code", exitCode);
        attr.put("remote_scheduler_host", remoteSchedulerHost);
        attr.put("remote_scheduler_port", remoteSchedulerPort);
        attr.put("scheduler_id", schedulerId);
        return attr;
    }

    public boolean isEqual(SchedulerEvent eActive) {
        boolean erg = true;
        Iterator <String>iProperties = properties().keySet().iterator();
        while (iProperties.hasNext()) {
            String trigger = iProperties.next();
            if (!"".equals(properties().get(trigger)) && eActive.properties().get(trigger) != null && !"expires".equalsIgnoreCase(trigger)
                    && !"created".equalsIgnoreCase(trigger) && !eActive.properties().get(trigger).equals(properties().get(trigger))) {
                erg = false;
            }
        }
        return erg;
    }

    public boolean isIn(LinkedHashSet <SchedulerEvent>listOfActiveEvents) {
        boolean erg = false;
        Iterator <SchedulerEvent> i = listOfActiveEvents.iterator();
        while (i.hasNext() && !erg) {
            if (this.isEqual(i.next())) {
                erg = true;
            }
        }
        return erg;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setLogic(String logic) {
        this.logic = logic;
    }

    public String getComment() {
        return comment;
    }

    public String getEventName() {
        if ("".equals(this.eventName)) {
            if ("".equals(this.eventClass)) {
                return this.eventId;
            } else {
                return this.eventClass + "." + this.eventId;
            }
        } else {
            return eventName;
        }
    }

}