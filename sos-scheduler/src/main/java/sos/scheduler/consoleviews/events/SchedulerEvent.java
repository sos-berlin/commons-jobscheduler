package sos.scheduler.consoleviews.events;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class SchedulerEvent {

    protected String event_title;
    protected String event_class;
    protected String event_id;
    protected String job_name;
    protected String job_chain;
    protected String order_id;
    protected String exit_code;
    protected String created;
    protected String expires;
    protected String remote_scheduler_host;
    protected String remote_scheduler_port;
    protected String scheduler_id;
    protected String logic = "";
    protected String comment = "";
    private String event_name;

    public String getEvent_title() {
        return event_title;
    }

    public String getEvent_class() {
        return event_class;
    }

    public String getEvent_id() {
        return event_id;
    }

    public String getJob_name() {
        return job_name;
    }

    public String getJob_chain() {
        return job_chain;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getExit_code() {
        return exit_code;
    }

    public String getCreated() {
        return created;
    }

    public String getExpires() {
        return expires;
    }

    public String getRemote_scheduler_host() {
        return remote_scheduler_host;
    }

    public String getRemote_scheduler_port() {
        return remote_scheduler_port;
    }

    public String getScheduler_id() {
        return scheduler_id;
    }

    public String getLogic() {
        return logic;
    }

    private HashMap properties() {
        HashMap attr = new HashMap();
        attr.put("event_title", event_title);
        attr.put("event_class", event_class);
        attr.put("event_id", event_id);
        attr.put("job_name", job_name);
        attr.put("job_chain", job_chain);
        attr.put("order_id", order_id);
        attr.put("exit_code", exit_code);
        attr.put("remote_scheduler_host", remote_scheduler_host);
        attr.put("remote_scheduler_port", remote_scheduler_port);
        attr.put("scheduler_id", scheduler_id);
        return attr;
    }

    public boolean isEqual(SchedulerEvent eActive) {
        boolean erg = true;
        Iterator iProperties = properties().keySet().iterator();
        while (iProperties.hasNext()) {
            String trigger = iProperties.next().toString();
            if (!"".equals(properties().get(trigger)) && eActive.properties().get(trigger) != null && !"expires".equalsIgnoreCase(trigger)
                    && !"created".equalsIgnoreCase(trigger) && !eActive.properties().get(trigger).equals(properties().get(trigger))) {
                erg = false;
            }
        }
        return erg;
    }

    public boolean isIn(LinkedHashSet listOfActiveEvents) {
        boolean erg = false;
        Iterator i = listOfActiveEvents.iterator();
        while (i.hasNext() && !erg) {
            if (this.isEqual((SchedulerEvent) i.next())) {
                erg = true;
            }
        }
        return erg;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public void setLogic(String logic) {
        this.logic = logic;
    }

    public String getComment() {
        return comment;
    }

    public String getEvent_name() {
        if ("".equals(this.event_name)) {
            if ("".equals(this.event_class)) {
                return this.event_id;
            } else {
                return this.event_class + "." + this.event_id;
            }
        } else {
            return event_name;
        }
    }

}