package com.sos.jobstreams.resolver;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.exception.SOSException;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateConfigurationException;
import com.sos.hibernate.exceptions.SOSHibernateFactoryBuildException;
import com.sos.hibernate.exceptions.SOSHibernateOpenSessionException;
import com.sos.jitl.eventhandler.handler.EventHandlerSettings;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.joc.exceptions.JocException;
 

public class TestEvents {

    
    private SOSHibernateSession getSession(String confFile) throws SOSHibernateFactoryBuildException, SOSHibernateOpenSessionException, SOSHibernateConfigurationException   {
        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
        sosHibernateFactory.addClassMapping(Constants.getConditionsClassMapping());
        sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());

        sosHibernateFactory.build();
        return sosHibernateFactory.openStatelessSession();
    }
    
     
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    @Ignore
    public void testInit() throws UnsupportedEncodingException, MalformedURLException, InterruptedException, SOSException, JocException, URISyntaxException, JAXBException     {
        EventHandlerSettings settings = new EventHandlerSettings();
        settings.setSchedulerId("scheduler_joc_cockpit");
        JSConditionResolver expressionResolver = new JSConditionResolver(getSession("src/test/resources/reporting.hibernate.cfg.xml"),null,settings );
        expressionResolver.init();
        expressionResolver.resolveInConditions();
    }

    @Test
    public void testGetEvents() throws UnsupportedEncodingException, MalformedURLException, InterruptedException, SOSException, URISyntaxException     {
        JSEvents jsEvents = new JSEvents();
        JSEvent jsEvent = new JSEvent();
        jsEvent.setEvent("test");
        jsEvent.setGlobalEvent(false);
        jsEvent.setJobStream("ssss");
        jsEvent.setOutConditionId(333L);
        jsEvent.setSession("10.10");
        jsEvent.setSchedulerId("scheduler_joc_cockpit");
        jsEvents.addEvent(jsEvent);

        JSEvent jsEvent2 = new JSEvent();
        jsEvent2.setEvent("test");
        jsEvent2.setGlobalEvent(true);
        jsEvent2.setJobStream("ssss");
        jsEvent2.setOutConditionId(333L);
        jsEvent2.setSession("10.10");
        jsEvent2.setSchedulerId("myScheduler");
        jsEvents.addEvent(jsEvent2);

        JSEventKey jsEventKey = new JSEventKey();
        jsEventKey.setEvent("test");
        jsEventKey.setSession("*");
        jsEventKey.setSchedulerId("scheduler_joc_cockpit");
        jsEventKey.setGlobalEvent(false);
        jsEventKey.setJobStream("");
        jsEvent = jsEvents.getEventByJobStream(jsEventKey);
        assertEquals("testGetEvents", "test", jsEvent.getEvent());

        jsEventKey.setGlobalEvent(true);
        jsEvent = jsEvents.getEventByJobStream(jsEventKey);
        assertEquals("testGetEvents", "test", jsEvent.getEvent());

        jsEventKey = new JSEventKey();
        jsEventKey.setEvent("test");
        jsEventKey.setJobStream("xssss");
        jsEventKey.setSession("*");
        jsEventKey.setSchedulerId("scheduler_joc_cockpit");
        jsEventKey.setGlobalEvent(false);
        jsEvent = jsEvents.getEventByJobStream(jsEventKey);
        assertEquals("testGetEvents", "test", jsEvent.getEvent());
        
        jsEventKey.setJobStream("");
        jsEvent = jsEvents.getEventByJobStream(jsEventKey);
        assertEquals("testGetEvents", "test", jsEvent.getEvent());

       
         
             
        
    }

    
}
