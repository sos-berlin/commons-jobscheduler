package com.sos.scheduler.model.objects;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.JodaTools;

public class JSObjMonthdaysTest {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(JSObjMonthdaysTest.class);

    private final static DateTimeFormatter fmtDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private final static DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");

    private final static DateTime baseDate = new DateTime(2012, 3, 12, 0, 0, 0, 0);
    private final static Interval baseInterval = new Interval(JodaTools.getStartOfMonth(baseDate), JodaTools.getEndOfMonth(baseDate));

    private static SchedulerObjectFactory factory = null;

    private String xml;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        factory = new SchedulerObjectFactory("8of9.sos", 4210);
        factory.initMarshaller(Spooler.class);
    }

    @Test
    public final void testSetValidXmlContent() {
        String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
        xml = "<run_time xmlns:ns2=\"job-chain-extensions\">\n" + "    <monthdays>\n" + "        <weekday day=\"tuesday\" which=\"2\">\n"
                + "            <period single_start=\"15:00\"/>\n" + "        </weekday>\n" + "    </monthdays>\n" + "</run_time>\n";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        assertEquals(xmlHeader + xml, runtime.toXMLString());
    }

    @Test
    public final void testWeekday() {
        xml = "<run_time>" + "<monthdays>" + "<weekday day=\"tuesday thursday\" which=\"2\">" + "<period single_start=\"15:00\" />" + "</weekday>"
                + "</monthdays>" + "</run_time>";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);

        Iterator<Object> it = runtime.getMonthdays().getDayOrWeekday().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Monthdays.Weekday) {
                Monthdays.Weekday d = (Monthdays.Weekday) o;
                JSObjWeekday weekday = new JSObjWeekday(factory);
                weekday.setObjectFieldsFrom(d);
                List<DateTime> result = weekday.getRunTimeElements(baseInterval).getStartTimes();
                assertEquals(2, result.size());

                DateTime dt = result.get(0);
                assertEquals(DateTimeConstants.THURSDAY, dt.getDayOfWeek());
                assertEquals("2012-03-08 15:00:00", fmtDateTime.print(dt));

                dt = result.get(1);
                assertEquals(DateTimeConstants.TUESDAY, dt.getDayOfWeek());
                assertEquals("2012-03-13 15:00:00", fmtDateTime.print(dt));
            }
        }
    }

    @Test
    public final void testWeekday2() {
        xml = "<run_time>" + "<monthdays>" + "<weekday day=\"tuesday\" which=\"-2\">" + "<period single_start=\"15:00\" />" + "</weekday>"
                + "<weekday day=\"monday\" which=\"-3\">" + "<period single_start=\"13:00\" />" + "</weekday>" + "</monthdays>" + "</run_time>";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);

        Iterator<Object> it = runtime.getMonthdays().getDayOrWeekday().iterator();
        List<JSObjWeekday> weekdays = new ArrayList<JSObjWeekday>();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Monthdays.Weekday) {
                Monthdays.Weekday d = (Monthdays.Weekday) o;
                JSObjWeekday weekday = new JSObjWeekday(factory);
                weekday.setObjectFieldsFrom(d);
                weekdays.add(weekday);
            }
        }
        assertEquals(2, weekdays.size());

        // Interval from 2012-03-19 to 2012-06-18
        Interval i = new Interval(baseInterval.getStart().plusDays(18), baseInterval.getEnd().plusMonths(3).minusDays(12));
        assertEquals("2012-03-19", fmtDate.print(i.getStart()));
        assertEquals("2012-06-18", fmtDate.print(i.getEnd()));

        JSObjWeekday w = weekdays.get(0);
        List<DateTime> result = w.getRunTimeElements(i).getStartTimes();
        assertEquals(3, result.size());
        assertEquals("2012-03-20 15:00:00", fmtDateTime.print(result.get(0)));
        assertEquals("2012-04-17 15:00:00", fmtDateTime.print(result.get(1)));
        assertEquals("2012-05-22 15:00:00", fmtDateTime.print(result.get(2)));

        w = weekdays.get(1);
        result = w.getRunTimeElements(i).getStartTimes();
        assertEquals(3, result.size());
        assertEquals("2012-04-16 13:00:00", fmtDateTime.print(result.get(0)));
        assertEquals("2012-05-14 13:00:00", fmtDateTime.print(result.get(1)));
        assertEquals("2012-06-11 13:00:00", fmtDateTime.print(result.get(2)));

    }

    @Test
    public final void testMonthdaysDay() {
        xml = "<run_time>" + "<monthdays>" + "<day day=\"5\">" + "<period single_start=\"15:00\" />" + "</day>" + "<day day=\"0\">"
                + "<period single_start=\"11:00\" />" + "</day>" + "</monthdays>" + "</run_time>";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);

        Iterator<Object> it = runtime.getMonthdays().getDayOrWeekday().iterator();
        List<JSObjMonthdaysDay> days = new ArrayList<JSObjMonthdaysDay>();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Monthdays.Day) {
                Monthdays.Day d = (Monthdays.Day) o;
                JSObjMonthdaysDay day = new JSObjMonthdaysDay(factory);
                day.setObjectFieldsFrom(d);
                days.add(day);
            }
        }
        assertEquals(2, days.size());

        // Interval from 2012-03-19 to 2012-06-18
        Interval i = new Interval(baseInterval.getStart().plusDays(18), baseInterval.getEnd().plusMonths(3).minusDays(12));
        assertEquals("2012-03-19", fmtDate.print(i.getStart()));
        assertEquals("2012-06-18", fmtDate.print(i.getEnd()));

        JSObjMonthdaysDay w = days.get(0);
        List<DateTime> result = w.getRunTimeElements(i).getStartTimes();
        assertEquals(3, result.size());
        assertEquals("2012-04-05 15:00:00", fmtDateTime.print(result.get(0)));
        assertEquals("2012-05-05 15:00:00", fmtDateTime.print(result.get(1)));
        assertEquals("2012-06-05 15:00:00", fmtDateTime.print(result.get(2)));

        w = days.get(1);
        result = w.getRunTimeElements(i).getStartTimes();
        assertEquals(3, result.size());
        assertEquals("2012-03-31 11:00:00", fmtDateTime.print(result.get(0)));
        assertEquals("2012-04-30 11:00:00", fmtDateTime.print(result.get(1)));
        assertEquals("2012-05-31 11:00:00", fmtDateTime.print(result.get(2)));

    }

    @Test
    public final void testMonthdays() {
        xml = "<run_time>" + "<monthdays>" + "<weekday day=\"tuesday\" which=\"-2\">" + "<period single_start=\"15:00\" />" + "</weekday>" + "<day day=\"0\">"
                + "<period single_start=\"23:59:59\" />" + "</day>" + "</monthdays>" + "</run_time>";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);

        List<DateTime> dates = runtime.getJsObjMonthdays().getRunTimeElements(baseInterval).getStartTimes();
        assertEquals(2, dates.size());
        assertEquals("2012-03-20 15:00:00", fmtDateTime.print(dates.get(0)));
        assertEquals("2012-03-31 23:59:59", fmtDateTime.print(dates.get(1)));

    }

}
