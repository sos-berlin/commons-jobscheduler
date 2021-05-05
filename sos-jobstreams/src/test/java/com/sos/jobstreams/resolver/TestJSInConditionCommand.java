package com.sos.jobstreams.resolver;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.sos.jobstreams.classes.JobStarter;

 

public class TestJSInConditionCommand {

    @Test
    public void testGetMapOfAttributes() {
        JobStarter jobstarter = new JobStarter();
        Map<String,String> listOfAttributes;
        listOfAttributes = jobstarter.testGetMapOfAttributes(null);
        assertEquals("testGetMapOfAttributes", "now", listOfAttributes.get("at"));
        listOfAttributes = jobstarter.testGetMapOfAttributes("at=now+20");
        assertEquals("testGetMapOfAttributes", "now+20", listOfAttributes.get("at"));
        listOfAttributes = jobstarter.testGetMapOfAttributes("now+20");
        assertEquals("testGetMapOfAttributes", "now+20", listOfAttributes.get("at"));
        listOfAttributes = jobstarter.testGetMapOfAttributes("at=now,force=yes");
        assertEquals("testGetMapOfAttributes", "now", listOfAttributes.get("at"));
        assertEquals("testGetMapOfAttributes", "yes", listOfAttributes.get("force"));
        listOfAttributes = jobstarter.testGetMapOfAttributes("now+10,force=no");
        assertEquals("testGetMapOfAttributes", "no", listOfAttributes.get("force"));
        assertEquals("testGetMapOfAttributes", "now+10", listOfAttributes.get("at"));
        }

}
