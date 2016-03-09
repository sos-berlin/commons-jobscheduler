package com.sos.scheduler.model.objects;

import com.sos.scheduler.model.SchedulerObjectFactory;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class JSObjParamsTest {

    private final static Logger logger = Logger.getLogger(JSObjParamsTest.class);

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
    public final void testAdd() {
        JSObjParams params = new JSObjParams(factory);
        params.add("param1", "value1");
        params.add("param2", "value2");
        testAssertions(params);
    }

    @Test
    public final void testUnmarshal() {
        JSObjParams params = new JSObjParams(factory, (Params) factory.unMarshall(xml));
        testAssertions(params);
    }

    @Test
    public final void testParamsFromString() {
        JSObjParams params = factory.createParams();
        params.setParamsFromString(xml);
        testAssertions(params);
    }

    @Test
    public final void testMerge() {
        JSObjParams params1 = factory.createParams();
        params1.setParamsFromString(xml);
        JSObjParams params2 = factory.createParams();
        params2.setParamsFromString(xmlToMerge);
        params1.merge(params2);
        testAssertions(params1);
        assertTrue(params1.hasParameterValue("param3"));
        assertEquals("value3", params1.getParameterValue("param3"));
    }

    private void testAssertions(JSObjParams params) {
        assertTrue(params.hasParameterValue("param1"));
        assertEquals("value1", params.getParameterValue("param1"));
        assertTrue(params.hasParameterValue("param2"));
        assertEquals("value2", params.getParameterValue("param2"));
    }
}
