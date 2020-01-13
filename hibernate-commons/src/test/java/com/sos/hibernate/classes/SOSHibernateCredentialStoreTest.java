package com.sos.hibernate.classes;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSHibernateCredentialStoreTest {

    final static Logger LOGGER = LoggerFactory.getLogger(SOSHibernateCredentialStoreTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @Ignore
    @Test
    public void csTest() throws Exception {
        String configFile = "./src/test/resources/hibernate.cs.cfg.xml";

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = new SOSHibernateFactory(configFile);
            factory.build();

            LOGGER.info(factory.getDialect().toString());
            session = factory.openStatelessSession();

        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }

    }

}
