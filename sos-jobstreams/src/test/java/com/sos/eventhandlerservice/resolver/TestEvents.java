package com.sos.eventhandlerservice.resolver;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.eventhandlerservice.classes.Constants;
import com.sos.exception.SOSException;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateConfigurationException;
import com.sos.hibernate.exceptions.SOSHibernateFactoryBuildException;
import com.sos.hibernate.exceptions.SOSHibernateOpenSessionException;
import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.reporting.db.DBLayer;
 

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
    public void testInit() throws UnsupportedEncodingException, MalformedURLException, InterruptedException, SOSException, URISyntaxException     {
        File f = new File("src/test/resources/config/private/private.conf");
        EventHandlerSettings settings = new EventHandlerSettings();
        settings.setJocUrl("http://localhost:4446");
        settings.setSchedulerId("scheduler_joc_cockpit");
        JSConditionResolver expressionResolver = new JSConditionResolver(getSession("src/test/resources/reporting.hibernate.cfg.xml"),f,settings );
        expressionResolver.init();
        expressionResolver.resolveInConditions();
    }

}