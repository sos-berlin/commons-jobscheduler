package com.sos.jobstreams.resolver;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;


public class TestJSInConditionCommand {

    @Test
    public void testGetMapOfAttributes() {
        JSInConditionCommand jsInConditionCommand = new JSInConditionCommand();
        Map<String,String> listOfAttributes;
        listOfAttributes = jsInConditionCommand.testGetMapOfAttributes(null);
        assertEquals("testGetMapOfAttributes", "now", listOfAttributes.get("at"));
        listOfAttributes = jsInConditionCommand.testGetMapOfAttributes("at=now+20");
        assertEquals("testGetMapOfAttributes", "now+20", listOfAttributes.get("at"));
        listOfAttributes = jsInConditionCommand.testGetMapOfAttributes("now+20");
        assertEquals("testGetMapOfAttributes", "now+20", listOfAttributes.get("at"));
        listOfAttributes = jsInConditionCommand.testGetMapOfAttributes("at=now,force=yes");
        assertEquals("testGetMapOfAttributes", "now", listOfAttributes.get("at"));
        assertEquals("testGetMapOfAttributes", "yes", listOfAttributes.get("force"));
        listOfAttributes = jsInConditionCommand.testGetMapOfAttributes("now+10,force=no");
        assertEquals("testGetMapOfAttributes", "no", listOfAttributes.get("force"));
        assertEquals("testGetMapOfAttributes", "now+10", listOfAttributes.get("at"));
        }

}
