package com.sos.jobstreams.resolver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sos.jobstreams.classes.Constants;
import com.sos.jobstreams.classes.EventDate;
import com.sos.jobstreams.db.DBItemEvent;
import com.sos.jobstreams.db.DBItemOutCondition;
import com.sos.jobstreams.db.DBLayerEvents;
import com.sos.jobstreams.db.FilterEvents;
import com.sos.jobstreams.resolver.interfaces.IJSCondition;
import com.sos.jobstreams.resolver.interfaces.IJSJobConditionKey;

import sos.util.SOSString;

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

    public boolean storeOutConditionEvent(SOSHibernateSession sosHibernateSession, DBItemEvent itemEvent) throws SOSHibernateException {
        sosHibernateSession.setAutoCommit(false);

        LOGGER.debug("create event ------>" + SOSString.toString(itemEvent));
        boolean ok = false;

        try {

            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            JSEvent event = new JSEvent();
            event.setCreated(new Date());
            event.setEvent(itemEvent.getEvent());
            event.setSession(itemEvent.getSession());
            event.setJobStream(itemEvent.getJobStream());
            event.setSchedulerId(this.jobSchedulerId);
            event.setOutConditionId(itemEvent.getOutConditionId());

            sosHibernateSession.beginTransaction();
            dbLayerEvents.store(event);
            sosHibernateSession.commit();
            ok = true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.debug("event " + itemEvent.getEvent() + " added to EventQueue");
            sosHibernateSession.rollback();
        }
        return ok;

    }

    public boolean deleteOutConditionEvent(SOSHibernateSession sosHibernateSession, FilterEvents filter) throws SOSHibernateException {
        sosHibernateSession.setAutoCommit(false);

        LOGGER.debug("delete event ------>" + SOSString.toString(filter));
        boolean ok = false;
        try {

            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerEvents.delete(filter);
            sosHibernateSession.commit();
            ok = true;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            sosHibernateSession.rollback();
        }

        return ok;

    }

    public void storeOutConditionEvents(SOSHibernateSession sosHibernateSession, JSEvents jsEvents, JSEvents jsAddEvents, JSEvents jsRemoveEvents)
            throws SOSHibernateException {
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

            if (outConditionEvent.isCreateCommand()) {
                boolean ok = storeOutConditionEvent(sosHibernateSession, itemEvent);
                jsEvents.addEvent(event);
                jsAddEvents.addEvent(event);
            } else {
                if (outConditionEvent.isDeleteCommand()) {
                    JSCondition jsCondition = new JSCondition(outConditionEvent.getEventValue());

                    FilterEvents filterEvent = new FilterEvents();
                    filterEvent.setEvent(jsCondition.getEventName());
                    EventDate eventDate = new EventDate();
                    filterEvent.setSession(eventDate.getEventDate(jsCondition.getConditionDate()));

                    filterEvent.setSchedulerId(jobSchedulerId);
                    filterEvent.setJobStream(jsCondition.getConditionJobStream());

                    boolean ok = deleteOutConditionEvent(sosHibernateSession, filterEvent);

                    JSEventKey eventKey = new JSEventKey();
                    eventKey.setEvent(filterEvent.getEvent());
                    eventKey.setSession(filterEvent.getSession());
                    eventKey.setJobStream(filterEvent.getJobStream());
                    event = jsEvents.getEvent(eventKey);
                    jsRemoveEvents.addEvent(event);
                    jsEvents.removeEvent(eventKey);
                }
            }
        }
    }

    public String getJobStream() {
        return jobStream;
    }

}
