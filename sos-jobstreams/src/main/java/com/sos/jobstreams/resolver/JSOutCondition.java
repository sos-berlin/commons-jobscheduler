package com.sos.jobstreams.resolver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jitl.jobstreams.classes.JSEvent;
import com.sos.jitl.jobstreams.db.DBItemEvent;
import com.sos.jitl.jobstreams.db.DBItemJobStreamHistory;
import com.sos.jitl.jobstreams.db.DBItemOutCondition;
import com.sos.jitl.jobstreams.interfaces.IJSJobConditionKey;
import com.sos.jobstreams.resolver.interfaces.IJSCondition;

public class JSOutCondition implements IJSJobConditionKey, IJSCondition {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSOutCondition.class);
    private Long id;
    private String jobSchedulerId;
    private String job;
    private String expression;
    private String jobStream;
    private List<JSOutConditionEvent> listOfOutConditionEvents;

    public JSOutCondition() {
        super();
        this.listOfOutConditionEvents = new ArrayList<JSOutConditionEvent>();
    }

    public void setItemOutCondition(DBItemOutCondition itemOutCondition) {
        this.id = itemOutCondition.getId();
        this.jobSchedulerId = itemOutCondition.getJobSchedulerId();
        this.job = itemOutCondition.getJob();
        this.expression = itemOutCondition.getExpression();
        this.jobStream = itemOutCondition.getJobStream();
    }

    public Long getId() {
        return this.id;
    }

    public String getJobSchedulerId() {
        return this.jobSchedulerId;
    }

    public String getJob() {
        return this.job;
    }

    public String getExpression() {
        return this.expression;
    }

    public void addEvent(JSOutConditionEvent outConditionEvent) {
        listOfOutConditionEvents.add(outConditionEvent);
    }

    public List<JSOutConditionEvent> getListOfOutConditionEvent() {
        return listOfOutConditionEvents;
    }

    public boolean storeOutConditionEvents(SOSHibernateSession sosHibernateSession, String session, DBItemJobStreamHistory historyEntry,
            JSEvents jsEvents, JSEvents jsNewEvents, JSEvents jsRemoveEvents) throws SOSHibernateException {
        boolean dbChange = false;
        sosHibernateSession.setAutoCommit(false);
        for (JSOutConditionEvent outConditionEvent : this.getListOfOutConditionEvent()) {
            JSCondition jsCondition = new JSCondition(outConditionEvent.getEventValue());
          
            DBItemEvent itemEvent = new DBItemEvent();
            itemEvent.setCreated(new Date());
            itemEvent.setJobStream(this.jobStream);
            itemEvent.setOutConditionId(outConditionEvent.getOutConditionId());
            itemEvent.setGlobalEvent(outConditionEvent.isGlobal());
            if (historyEntry != null) {
                itemEvent.setJobStreamHistoryId(historyEntry.getId());
            } else {
                LOGGER.debug("unknown historyId for " + session + " Maybe a simulated out condition");
                itemEvent.setJobStreamHistoryId(0L);
            }
          
            if (jsCondition.isHaveDate()) {
                itemEvent.setSession(jsCondition.getConditionDate());
                itemEvent.setEvent(jsCondition.getEventName());
            } else {
                itemEvent.setEvent(outConditionEvent.getEventValue());
                itemEvent.setSession(session);
            }
            
            if (outConditionEvent.isDeleteCommand()) {
                if (jsCondition.getConditionJobStream() != "") {
                    itemEvent.setJobStream(jsCondition.getConditionJobStream());
                }   
            }
            
            boolean b=false;
            if (jsCondition.isHaveDate()) {
                b =  executStoreOutConditionEvents(sosHibernateSession,outConditionEvent,itemEvent,historyEntry,jsEvents,jsNewEvents,jsRemoveEvents);
                dbChange = b || dbChange;
            } else {
                b = executStoreOutConditionEvents(sosHibernateSession,outConditionEvent,itemEvent,historyEntry,jsEvents,jsNewEvents,jsRemoveEvents);
                dbChange = b || dbChange;
                itemEvent.setSession(Constants.getSession());
                executStoreOutConditionEvents(sosHibernateSession,outConditionEvent,itemEvent,historyEntry,jsEvents,jsNewEvents,jsRemoveEvents);
                dbChange = b || dbChange;
            }
        }
        return dbChange;
    }

    public boolean executStoreOutConditionEvents(SOSHibernateSession sosHibernateSession,JSOutConditionEvent outConditionEvent,DBItemEvent itemEvent, DBItemJobStreamHistory historyEntry,
            JSEvents jsEvents, JSEvents jsNewEvents, JSEvents jsRemoveEvents) throws SOSHibernateException {
        boolean dbChange = false;
        sosHibernateSession.setAutoCommit(false);
           
            JSEvent event = new JSEvent();
            event.setItemEvent(itemEvent);
            event.setSchedulerId(jobSchedulerId);

            LOGGER.trace("Check existing of event:");
            boolean eventExist = (jsEvents.getEvent(event.getKey()) != null);
            LOGGER.trace("JobSchedulerId:" + event.getKey().getSchedulerId() + " jobstream:" + event.getKey().getJobStream() + " session:" + event.getKey().getSession() + " event:" + event.getKey().getEvent() + " --> " + eventExist );

            if (outConditionEvent.isCreateCommand()) {
                if (!eventExist) {
                    LOGGER.trace("---> Add event: " + event.getSession() + " " + event.getEvent());

                    jsEvents.addEvent(event);
                    jsNewEvents.addEvent(event);
                    dbChange = !event.store(sosHibernateSession);
                }
            } else {
                if (outConditionEvent.isDeleteCommand() && eventExist) {
                   
                    LOGGER.trace("---> Delete event: " + event.getSession() + " " + event.getEvent());
                    jsEvents.removeEvent(event);
                    jsRemoveEvents.addEvent(event);
                    dbChange = !event.deleteEvent(sosHibernateSession);
                }
            
        }
        return dbChange;
    }
    
    public String getJobStream() {
        return jobStream;
    }

}
