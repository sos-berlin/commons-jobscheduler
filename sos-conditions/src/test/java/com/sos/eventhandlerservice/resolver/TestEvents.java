package com.sos.eventhandlerservice.resolver;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.eventhandlerservice.classes.Constants;
import com.sos.exception.SOSException;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateConfigurationException;
import com.sos.hibernate.exceptions.SOSHibernateFactoryBuildException;
import com.sos.hibernate.exceptions.SOSHibernateOpenSessionException;
 
@Ignore
public class TestEvents {

    
    private SOSHibernateSession getSession(String confFile) throws SOSHibernateFactoryBuildException, SOSHibernateOpenSessionException, SOSHibernateConfigurationException   {
        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
        sosHibernateFactory.addClassMapping(Constants.getConditionsClassMapping());
        sosHibernateFactory.build();
        return sosHibernateFactory.openStatelessSession();
    }
    
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

    @Ignore
    @Test
    public void testInit() throws UnsupportedEncodingException, MalformedURLException, InterruptedException, SOSException, URISyntaxException     {
        JSConditionResolver expressionResolver = new JSConditionResolver(getSession("src/test/resources/reporting.hibernate.cfg.xml"));
        expressionResolver.init();
        expressionResolver.resolveInConditions();
    }

}
