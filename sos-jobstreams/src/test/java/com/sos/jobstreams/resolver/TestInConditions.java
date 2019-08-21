package com.sos.jobstreams.resolver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.hibernate.exceptions.SOSHibernateException;


public class TestInConditions {

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
