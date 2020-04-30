package com.sos.jobstreams.resolver;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateConfigurationException;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.exceptions.SOSHibernateFactoryBuildException;
import com.sos.hibernate.exceptions.SOSHibernateOpenSessionException;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jitl.jobstreams.classes.JSEvent;
import com.sos.jitl.jobstreams.db.DBItemEvent;
import com.sos.jitl.jobstreams.db.DBLayerConsumedInConditions;
import com.sos.jitl.jobstreams.db.DBLayerEvents;
import com.sos.jitl.jobstreams.db.FilterConsumedInConditions;
import com.sos.jitl.jobstreams.db.FilterEvents;

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

        JSEvent event = new JSEvent();

        try {
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);

            event.setCreated(new Date());
            event.setEvent("test");
            event.setSession("08.22");
            event.setJobStream("myStream");

            dbLayerEvents.store(event);
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
            filterConsumedInConditions.setJobStream("test");
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
            Constants.periodBegin = "00:00";
            filter.setSession(Constants.getSession());
            filter.setSchedulerId("scheduler_joc_cockpit");
            filter.setJobStream("test");
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerEvents.deleteEventsWithOutConditions(filter);
            sosHibernateSession.commit();
        } catch (Exception e) {
            sosHibernateSession.rollback();
        }
    }
}
