package com.sos.scheduler.model.objects;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSObjOrderTest {

    private final static Logger logger = Logger.getLogger(JSObjOrderTest.class);

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
        // xml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
        // "<order job_chain=\"test_chain\" id=\"I\">" +
        // "<run_time let_run=\"no\">" +
        // "<weekdays>" +
        // "<day day=\"1\"/>" +
        // "<day day=\"3\"/>" +
        // "</weekdays>" +
        // "</run_time>" +
        // "</order>";

        JSObjOrder order = new JSObjOrder(factory);
        order.getOrderFromXMLString(xml);
        // logger.debug(order.toXMLString());
        JSObjRunTime r = order.getJSObjRunTime();
        List<DateTime> d = r.getDtSingleStarts(IntervalConstants.NEXT_WEEK.getInterval());
        logger.debug(d.size());
        assertEquals(xml, order.toXMLString());

        // DateTimeFormatter fmtDateTime =
        // DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        // Iterator<RunTime.Date> itD = runtime.getDate().iterator();
        // while(itD.hasNext()) {
        // RunTime.Date d = itD.next();
        // JSObjDate date = new JSObjDate(factory);
        // date.setObjectFieldsFrom(d);
        // Iterator<DateTime> it = date.getDtSingleStarts(next24H).iterator();
        // while(it.hasNext()) {
        // DateTime dt = it.next();
        // logger.debug(fmtDateTime.print(dt));
        // }
        // }
    }

}
