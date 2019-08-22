package com.sos.jobstreams.resolver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sos.jobstreams.classes.Constants;
import com.sos.jobstreams.classes.EventDate;
import com.sos.jobstreams.db.DBItemEvent;
import com.sos.jobstreams.db.DBItemOutCondition;
import com.sos.jobstreams.db.DBLayerEvents;
import com.sos.jobstreams.db.FilterEvents;
import com.sos.jobstreams.resolver.interfaces.IJSCondition;
import com.sos.jobstreams.resolver.interfaces.IJSJobConditionKey;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public JSEvent storeOutConditionEvent(SOSHibernateSession sosHibernateSession, DBItemEvent itemEvent) throws SOSHibernateException {
        sosHibernateSession.setAutoCommit(false);

        JSEvent event = new JSEvent();

        event.setItemEvent(itemEvent);
        LOGGER.debug("create event ------>" + event.getEvent());

        try {
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerEvents.store(itemEvent);
            sosHibernateSession.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            sosHibernateSession.rollback();
        }
        return event;

    }

    public void deleteOutConditionEvent(SOSHibernateSession sosHibernateSession, FilterEvents filter) throws SOSHibernateException {
        sosHibernateSession.setAutoCommit(false);

        LOGGER.debug("delete event ------>" + filter.getEvent());

        try {

            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerEvents.delete(filter);
            sosHibernateSession.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            sosHibernateSession.rollback();
        }

    }

    public void storeOutConditionEvents(SOSHibernateSession sosHibernateSession, JSEvents jsEvents) throws SOSHibernateException {
        for (JSOutConditionEvent outConditionEvent : this.getListOfOutConditionEvent()) {
            sosHibernateSession.setAutoCommit(false);


            if (outConditionEvent.isCreateCommand()) {
                DBItemEvent itemEvent = new DBItemEvent();
                itemEvent.setCreated(new Date());
                itemEvent.setEvent(outConditionEvent.getEventValue());
                itemEvent.setOutConditionId(outConditionEvent.getOutConditionId());
                itemEvent.setJobStream(this.jobStream);
                itemEvent.setSession(Constants.getSession());
                JSEvent event = storeOutConditionEvent(sosHibernateSession, itemEvent);
                jsEvents.addEvent(event);
            } else {
                if (outConditionEvent.isDeleteCommand()) {
                    JSCondition jsCondition = new JSCondition(outConditionEvent.getEventValue());

                    FilterEvents filterEvent = new FilterEvents();
                    filterEvent.setEvent(jsCondition.getEventName());
                    EventDate eventDate = new EventDate();

                    filterEvent.setSchedulerId(jobSchedulerId);
                    filterEvent.setSession(eventDate.getEventDate(jsCondition.getConditionDate()));
                    filterEvent.setJobStream(jsCondition.getConditionJobStream());

                    deleteOutConditionEvent(sosHibernateSession, filterEvent);
                    JSEventKey eventKey = new JSEventKey();
                    eventKey.setEvent(filterEvent.getEvent());
                    eventKey.setSession(filterEvent.getSession());
                    eventKey.setJobStream(filterEvent.getJobStream());
                    jsEvents.removeEvent(eventKey);
                }
            }
        }
    }

    public String getJobStream() {
        return jobStream;
    }

}
