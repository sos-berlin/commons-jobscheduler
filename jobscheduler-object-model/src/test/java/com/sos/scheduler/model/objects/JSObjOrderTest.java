package com.sos.scheduler.model.objects;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSObjOrderTest {

    private static final Logger LOGGER = Logger.getLogger(JSObjOrderTest.class);
    private static SchedulerObjectFactory factory = null;
    private String xml;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        factory = new SchedulerObjectFactory("8of9.sos", 4210);
        factory.initMarshaller(Spooler.class);
    }

    @Test
    public final void testSetXml() {
        xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<order xmlns:ns2=\"job-chain-extensions\">\n" + "    <params>\n"
                + "        <param name=\"successor\" value=\"\"/>\n" + "        <param name=\"predecessor\" value=\"H,folder1/J\"/>\n"
                + "        <param name=\"command\" value=\"ping -n 20 localhost\"/>\n" + "    </params>\n" + "    <run_time let_run=\"no\">\n"
                + "        <weekdays>\n" + "            <day day=\"1\"/>\n" + "            <day day=\"3\"/>\n" + "        </weekdays>\n"
                + "    </run_time>\n" + "</order>\n";
        JSObjOrder order = new JSObjOrder(factory);
        order.getOrderFromXMLString(xml);
        JSObjRunTime r = order.getJSObjRunTime();
        List<DateTime> d = r.getDtSingleStarts(IntervalConstants.NEXT_WEEK.getInterval());
        LOGGER.debug(d.size());
        assertEquals(xml, order.toXMLString());
    }

}