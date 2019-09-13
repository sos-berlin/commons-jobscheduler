package com.sos.jobstreams.resolver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jobstreams.classes.Constants;
import com.sos.jobstreams.classes.EventDate;
import com.sos.jobstreams.db.DBItemEvent;
import com.sos.jobstreams.db.DBItemOutCondition;
import com.sos.jobstreams.db.DBLayerEvents;
import com.sos.jobstreams.db.FilterEvents;
import com.sos.jobstreams.resolver.interfaces.IJSCondition;
import com.sos.jobstreams.resolver.interfaces.IJSJobConditionKey;

import sos.util.SOSString;

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

    public boolean storeOutConditionEvents(SOSHibernateSession sosHibernateSession, JSEvents jsEvents, JSEvents jsNewEvents, JSEvents jsRemoveEvents)
            throws SOSHibernateException {
        boolean dbChange = false;
        for (JSOutConditionEvent outConditionEvent : this.getListOfOutConditionEvent()) {
            sosHibernateSession.setAutoCommit(false);

            DBItemEvent itemEvent = new DBItemEvent();
            itemEvent.setCreated(new Date());
            itemEvent.setEvent(outConditionEvent.getEventValue());
            itemEvent.setOutConditionId(outConditionEvent.getOutConditionId());
            itemEvent.setJobStream(this.jobStream);
            
            itemEvent.setSession(Constants.getSession());
            JSEvent event = new JSEvent();
            event.setItemEvent(itemEvent);
            event.setSchedulerId(jobSchedulerId);

            if (outConditionEvent.isCreateCommand()) {
                jsEvents.addEvent(event);
                jsNewEvents.addEvent(event);
                dbChange = !event.store(sosHibernateSession);
            } else {
                if (outConditionEvent.isDeleteCommand()) {
                    JSCondition jsCondition = new JSCondition(outConditionEvent.getEventValue());
                    EventDate eventDate = new EventDate();
                    event.setEvent(jsCondition.getEventName());
                    event.setSession(eventDate.getEventDate(jsCondition.getConditionDate()));
                    event.setJobStream(jsCondition.getConditionJobStream());
                    jsEvents.removeEvent(event);
                    jsRemoveEvents.addEvent(event);
                    dbChange = !event.deleteEvent(sosHibernateSession);
                }
            }
        }
        return dbChange;
    }

    public String getJobStream() {
        return jobStream;
    }

}
