package com.sos.scheduler.model.objects;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSObjDateTest {

    private final static Logger logger = Logger.getLogger(JSObjDateTest.class);

    private final static DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");

    private final static DateTime from = new DateTime(2012, 3, 12, 0, 0, 0, 0);
    private final static Interval march12th = new Interval(from, from.plusDays(1));
    private final static Interval next24H = IntervalConstants.NEXT_24H.getInterval();

    private static SchedulerObjectFactory factory = null;
    private static SchedulerObjectFactory factoryWithDefaultPeriod = null;

    private String xml;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        factory = new SchedulerObjectFactory();
        factory.initMarshaller(Spooler.class);
        factoryWithDefaultPeriod = new SchedulerObjectFactory();
        factoryWithDefaultPeriod.setUseDefaultPeriod(true);
        factoryWithDefaultPeriod.initMarshaller(Spooler.class);
    }

    @Test
    public final void testSetValidXmlContent() {
        String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
        xml =
                "<run_time xmlns:ns2=\"job-chain-extensions\">\n" + "    <date date=\"2012-03-12\">\n" + "        <period single_start=\"15:00\"/>\n"
                        + "    </date>\n" + "</run_time>\n";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        assertEquals(xmlHeader + xml, runtime.toXMLString());
    }

    @Test(expected = JobSchedulerException.class)
    public final void testSetInvalidXmlContent() {
        xml = "<invalid date />";
        JSObjPeriod period = new JSObjPeriod(factory, xml);
        logger.debug(period.toXMLString());
    }

    @Test
    public final void testSingleStarts() {
        xml =
                "<run_time>" + "<date date=\"2012-03-12\">" + "<period single_start=\"15:00\" />" + "<period single_start=\"19:00\" />"
                        + "<period single_start=\"11:00\" />" + "</date>" + "<date date=\"2012-03-10\">" + "<period single_start=\"10:00\" />"
                        + "<period single_start=\"09:00\" />" + "<period single_start=\"11:00\" />" + "</date>" + "</run_time>";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);

        DateTimeFormatter fmtDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        Iterator<RunTime.Date> itD = runtime.getDate().iterator();
        while (itD.hasNext()) {
            RunTime.Date d = itD.next();
            JSObjDate date = new JSObjDate(factory);
            date.setObjectFieldsFrom(d);
            Iterator<DateTime> it = date.getRunTimeElements(next24H).getStartTimes().iterator();
            while (it.hasNext()) {
                DateTime dt = it.next();
                logger.debug(fmtDateTime.print(dt));
            }
        }
    }

    @Test
    public final void testDateWithoutPeriod() {

        String dateString = fmtDate.print(march12th.getStart());
        xml = "<run_time>" + "<date date=\"" + dateString + "\"/>" + "</run_time>";

        JSObjRunTime runtime = new JSObjRunTime(factoryWithDefaultPeriod, xml);

        List<RunTime.Date> result = runtime.getDate();
        assertEquals(1, result.size());
        Iterator<RunTime.Date> itD = result.iterator();
        while (itD.hasNext()) {
            RunTime.Date d = itD.next();
            JSObjDate date = new JSObjDate(factoryWithDefaultPeriod);
            date.setObjectFieldsFrom(d);
            List<DateTime> startTimes = date.getRunTimeElements(march12th).getStartTimes();
            assertEquals(1, startTimes.size());
            Iterator<DateTime> it = startTimes.iterator();
            while (it.hasNext()) {
                DateTime dt = it.next();
                assertEquals("2012-03-12", fmtDate.print(dt));
            }
        }
    }

}
