package com.sos.eventhandlerservice.resolver;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.db.DBItemEvent;
import com.sos.eventhandlerservice.db.DBLayerConsumedInConditions;
import com.sos.eventhandlerservice.db.DBLayerEvents;
import com.sos.eventhandlerservice.db.FilterConsumedInConditions;
import com.sos.eventhandlerservice.db.FilterEvents;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateConfigurationException;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.exceptions.SOSHibernateFactoryBuildException;
import com.sos.hibernate.exceptions.SOSHibernateOpenSessionException;

@Ignore
public class TestEventsDb {

    @Ignore
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Ignore
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Ignore
    @Before
    public void setUp() throws Exception {
    }

    private SOSHibernateSession getSession(String confFile) throws SOSHibernateFactoryBuildException, SOSHibernateOpenSessionException,
            SOSHibernateConfigurationException {
        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
        sosHibernateFactory.addClassMapping(Constants.getConditionsClassMapping());
        sosHibernateFactory.build();
        return sosHibernateFactory.openStatelessSession();
    }

    @Ignore
    @Test
    public void testConditions() throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = getSession("src/test/resources/reporting.hibernate.cfg.xml");
        sosHibernateSession.setAutoCommit(false);

        DBItemEvent itemEvent = new DBItemEvent();
        JSEvent event = new JSEvent();
        itemEvent.setCreated(new Date());
        itemEvent.setEvent("test");
        itemEvent.setSession("now");
        event.setItemEvent(itemEvent);

        try {
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerEvents.store(itemEvent);
            sosHibernateSession.commit();
        } catch (Exception e) {
            sosHibernateSession.rollback();
        }
    }

    @Ignore
    @Test
    public void testDeleteInWorkflow() throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = getSession("src/test/resources/reporting.hibernate.cfg.xml");
        sosHibernateSession.setAutoCommit(false);

        try {
            FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
            filterConsumedInConditions.setSession("now");
            filterConsumedInConditions.setWorkflow("test");
            DBLayerConsumedInConditions dbLayerConsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerConsumedInConditions.deleteConsumedInConditions(filterConsumedInConditions);
            sosHibernateSession.commit();
        } catch (Exception e) {
            sosHibernateSession.rollback();
        }
    }

    @Ignore
    @Test
    public void testDeleteOutWorkflow() throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = getSession("src/test/resources/reporting.hibernate.cfg.xml");
        sosHibernateSession.setAutoCommit(false);

        try {
            FilterEvents filter = new FilterEvents();
            filter.setSession("now");
            filter.setWorkflow("test");
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerEvents.deleteEventsFromWorkflow(filter);
            sosHibernateSession.commit();
        } catch (Exception e) {
            sosHibernateSession.rollback();
        }
    }
}