package com.sos.eventhandlerservice.resolver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.classes.EventDate;
import com.sos.eventhandlerservice.db.DBItemEvent;
import com.sos.eventhandlerservice.db.DBItemOutCondition;
import com.sos.eventhandlerservice.db.DBLayerEvents;
import com.sos.eventhandlerservice.db.FilterEvents;
import com.sos.eventhandlerservice.resolver.interfaces.IJSCondition;
import com.sos.eventhandlerservice.resolver.interfaces.IJSJobConditionKey;
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
    private String workflow;
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
        this.workflow = itemOutCondition.getWorkflow();
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
                itemEvent.setWorkflow(this.workflow);
                itemEvent.setSession(Constants.getSession());
                JSEvent event = storeOutConditionEvent(sosHibernateSession, itemEvent);
                jsEvents.addEvent(event);
            } else {
                if (outConditionEvent.isDeleteCommand()) {
                    JSCondition jsCondition = new JSCondition(outConditionEvent.getEventValue());

                    FilterEvents filterEvent = new FilterEvents();
                    filterEvent.setEvent(jsCondition.getEventName());
                    EventDate eventDate = new EventDate();
                    filterEvent.setSession(eventDate.getEventDate(jsCondition.getConditionDate()));
                    filterEvent.setWorkflow(jsCondition.getConditionWorkflow());

                    deleteOutConditionEvent(sosHibernateSession, filterEvent);
                    JSEventKey eventKey = new JSEventKey();
                    eventKey.setEvent(filterEvent.getEvent());
                    eventKey.setSession(filterEvent.getSession());
                    eventKey.setWorkflow(filterEvent.getWorkflow());
                    jsEvents.removeEvent(eventKey);
                }
            }
        }
    }

    public String getWorkflow() {
        return workflow;
    }

}
