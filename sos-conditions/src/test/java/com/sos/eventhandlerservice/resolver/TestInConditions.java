package com.sos.eventhandlerservice.resolver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.hibernate.exceptions.SOSHibernateException;

@Ignore
public class TestInConditions {

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
    public void testConditions() throws SOSHibernateException {
        JSConditions jsConditions = new JSConditions();
        List<JSCondition> listOfConditions = jsConditions.getListOfConditions("event:event1 and event2 or not event:event3");
        assertEquals("testConditions", "event", listOfConditions.get(0).getConditionType());
        assertEquals("testConditions", "event1", listOfConditions.get(0).getConditionParam());
        assertEquals("testConditions", "event", listOfConditions.get(1).getConditionType());
        assertEquals("testConditions", "event2", listOfConditions.get(1).getConditionParam());
        assertEquals("testConditions", "event", listOfConditions.get(2).getConditionType());
        assertEquals("testConditions", "event3", listOfConditions.get(2).getConditionParam());

        
        listOfConditions = jsConditions.getListOfConditions("(event:event1 or event:event2) and event:event3");
        assertEquals("testConditions", "event", listOfConditions.get(0).getConditionType());
        assertEquals("testConditions", "event1", listOfConditions.get(0).getConditionParam());
        assertEquals("testConditions", "event", listOfConditions.get(1).getConditionType());
        assertEquals("testConditions", "event2", listOfConditions.get(1).getConditionParam());
        assertEquals("testConditions", "event", listOfConditions.get(2).getConditionType());
        assertEquals("testConditions", "event3", listOfConditions.get(2).getConditionParam());
    }

}
