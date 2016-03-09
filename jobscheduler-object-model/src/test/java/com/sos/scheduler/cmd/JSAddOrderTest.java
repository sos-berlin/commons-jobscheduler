package com.sos.scheduler.cmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjParams;
import com.sos.scheduler.model.objects.Params;
import com.sos.scheduler.model.objects.Spooler;

public class JSAddOrderTest {

    private final static Logger logger = Logger.getLogger(JSAddOrderTest.class);

    private static SchedulerObjectFactory factory = null;

    private final String xml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + "" + "<params>" + "<param name=\"param1\" value=\"value1\"/>"
            + "<param name=\"param2\" value=\"value2\"/>" + "</params>";
    private final String xmlToMerge = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + "" + "<params>"
            + "<param name=\"param2\" value=\"value2-modified\"/>" + "<param name=\"param3\" value=\"value3\"/>" + "</params>";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        factory = new SchedulerObjectFactory("localhost", 4210);
        factory.initMarshaller(Spooler.class);
    }

    @Test
    public void testMerge() {
        JSAddOrder cmdAdd = new JSAddOrder(factory, "myOrder", "myChain");
        JSObjParams params = new JSObjParams(factory, (Params) factory.unMarshall(xml));
        JSObjParams paramsToMerge = new JSObjParams(factory, (Params) factory.unMarshall(xmlToMerge));
        cmdAdd.setParams(params);
        cmdAdd.mergeParams(paramsToMerge);
        params = new JSObjParams(factory, (Params) factory.unMarshall(cmdAdd.getParams().toXMLString()));
        assertTrue(params.hasParameterValue("param1"));
        assertEquals("value1", params.getParameterValue("param1"));
        assertTrue(params.hasParameterValue("param2"));
        assertEquals("value2", params.getParameterValue("param2"));
        assertTrue(params.hasParameterValue("param3"));
        assertEquals("value3", params.getParameterValue("param3"));
    }

    @Test
    public void testNewParams() {
        JSAddOrder cmdAdd = new JSAddOrder(factory, "myOrder", "myChain");
        JSObjParams paramsToMerge = new JSObjParams(factory, (Params) factory.unMarshall(xmlToMerge));
        cmdAdd.mergeParams(paramsToMerge);
        JSObjParams params = new JSObjParams(factory, (Params) factory.unMarshall(cmdAdd.getParams().toXMLString()));
        assertTrue(params.hasParameterValue("param2"));
        assertEquals("value2-modified", params.getParameterValue("param2"));
        assertTrue(params.hasParameterValue("param3"));
        assertEquals("value3", params.getParameterValue("param3"));
    }

}
