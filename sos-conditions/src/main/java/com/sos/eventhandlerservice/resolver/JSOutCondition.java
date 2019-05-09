package com.sos.eventhandlerservice.resolver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sos.eventhandlerservice.db.DBItemEvent;
import com.sos.eventhandlerservice.db.DBItemOutCondition;
import com.sos.eventhandlerservice.db.DBLayerEvents;
import com.sos.eventhandlerservice.resolver.interfaces.IJSCondition;
import com.sos.eventhandlerservice.resolver.interfaces.IJSJobConditionKey;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;

public class JSOutCondition implements IJSJobConditionKey, IJSCondition {

    private Long id;
    private String masterId;
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
        this.masterId = itemOutCondition.getMasterId();
        this.job = itemOutCondition.getJob();
        this.expression = itemOutCondition.getExpression();
        this.workflow = itemOutCondition.getWorkflow();
    }

    public Long getId() {
        return this.id;
    }

    public String getMasterId() {
        return this.masterId;
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

    public void storeOutConditionEvents(SOSHibernateSession sosHibernateSession, JSEvents jsEvents) throws SOSHibernateException {
        for (JSOutConditionEvent outConditionEvent : this.getListOfOutConditionEvent()) {
            JSEvent event = new JSEvent();
            sosHibernateSession.setAutoCommit(false);

            DBItemEvent itemEvent = new DBItemEvent();
            itemEvent.setCreated(new Date());
            itemEvent.setEvent(outConditionEvent.getEvent());
            itemEvent.setSession("now");
            itemEvent.setOutConditionId(outConditionEvent.getOutConditionId());
            itemEvent.setWorkflow(this.workflow);
            event.setItemEvent(itemEvent);
            System.out.println("create event ------>" + event.getEvent());

            try {
                DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
                sosHibernateSession.beginTransaction();
                dbLayerEvents.store(itemEvent);
                sosHibernateSession.commit();
            } catch (Exception e) {
                sosHibernateSession.rollback();
            }
            jsEvents.addEvent(event);
        }
    }

    
    public String getWorkflow() {
        return workflow;
    }

}
