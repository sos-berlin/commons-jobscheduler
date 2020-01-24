package com.sos.scheduler.model.objects;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.TestBase;

public class JSObjRunTimeTest extends TestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjRunTimeTest.class);
    private static final DateTimeFormatter FMT_DATE_TIME = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FMT_DATE = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT_TIME = DateTimeFormat.forPattern("HH:mm:ss");
    private static final Interval NEXT_24H = IntervalConstants.NEXT_24H.getInterval();
    private static final Interval CURRENT_WEEK = IntervalConstants.CURRENT_WEEK.getInterval();
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
        xml = "<run_time single_start=\"15:00\" xmlns:ns2=\"job-chain-extensions\"/>\n";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        assertEquals(xmlHeader + xml, runtime.toXMLString());
    }

    @Test(expected = JobSchedulerException.class)
    public final void testSetInvalidXmlContent() {
        xml = "<invalid run_time />";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        LOGGER.debug(runtime.toXMLString());
    }

    @Test
    public final void testConstructorWithXmlContent() {
        String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
        xml = "<run_time single_start=\"15:00\" xmlns:ns2=\"job-chain-extensions\"/>\n";
        JSObjRunTime p = new JSObjRunTime(factory, xml);
        assertEquals(xmlHeader + xml, p.toXMLString());
    }

    @Test
    public final void testFlags() {
        xml = "<run_time single_start='15:00' />";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        assertEquals(false, runtime.hasAt());
        assertEquals(false, runtime.hasPeriod());
        assertEquals(false, runtime.hasDate());
        assertEquals(false, runtime.hasWeekdays());
        assertEquals(false, runtime.hasMonth());
        assertEquals(false, runtime.hasMonthdays());
        assertEquals(false, runtime.hasUltimos());
        assertEquals(true, runtime.hasHolidays());
        assertEquals(false, runtime.hasSubsequentRunTimes());
        xml = "<run_time begin='15:00' end='17:00' repeat='10:00' />";
        runtime = new JSObjRunTime(factory, xml);
        assertEquals(false, runtime.hasSubsequentRunTimes());
    }

    @Test
    public final void testFlags2() {
        xml = "<run_time />";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        assertEquals(false, runtime.hasAt());
        assertEquals(false, runtime.hasPeriod());
        assertEquals(false, runtime.hasDate());
        assertEquals(false, runtime.hasWeekdays());
        assertEquals(false, runtime.hasMonth());
        assertEquals(false, runtime.hasMonthdays());
        assertEquals(false, runtime.hasUltimos());
        assertEquals(true, runtime.hasHolidays());
        assertEquals(false, runtime.hasSubsequentRunTimes());
        xml = "<run_time begin='15:00' end='17:00' repeat='10:00' />";
        runtime = new JSObjRunTime(factory, xml);
        assertEquals(false, runtime.hasSubsequentRunTimes());
    }

    @Test
    public final void testSingleStart() {
        xml = "<run_time single_start='15:00' when_holiday='previous_non_holiday'/>";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        DateTime d = new DateTime(2012, 3, 7, 16, 0, 0, 0);
        assertEquals("2012-03-07 15:00", FMT_DATE_TIME.print(runtime.getRunTimePeriod().getDtSingleStartOrNull(d)));
        DateTime start = d.minusMillis(d.getMillisOfDay());
        Interval i = new Interval(start, start.plusDays(1));
        assertEquals("2012-03-07 15:00", FMT_DATE_TIME.print(runtime.getDtSingleStarts(i).get(0)));
    }

    @Test
    public final void testNextSingleStart() {
        DateTime single = new DateTime().plusMinutes(1);
        xml = "<run_time single_start='" + FMT_TIME.print(single) + "' />";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        assertEquals(FMT_DATE_TIME.print(single), FMT_DATE_TIME.print(runtime.getDtSingleStarts(NEXT_24H).get(0)));
        single = new DateTime().minusMinutes(1);
        xml = "<run_time single_start='" + FMT_TIME.print(single) + "' />";
        runtime = new JSObjRunTime(factory, xml);
        assertEquals(FMT_DATE_TIME.print(single.plusDays(1)), FMT_DATE_TIME.print(runtime.getDtSingleStarts(NEXT_24H).get(0)));
    }

    @Test
    public final void testBeginAndEnd() {
        xml = "<run_time begin='15:00' end='17:30'/>";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        DateTime d = new DateTime(2012, 3, 8, 16, 0, 0, 0);
        assertEquals("2012-03-08 15:00", FMT_DATE_TIME.print(runtime.getRunTimePeriod().getDtBeginOrNull(d)));
        assertEquals("2012-03-08 17:30", FMT_DATE_TIME.print(runtime.getRunTimePeriod().getDtEndOrNull(d)));
    }

    @Test
    public final void testPeriod() {
        xml = "<run_time begin='15:00' end='17:00' />";
        DateTime dIn = new DateTime(2012, 3, 7, 16, 0, 0, 0);
        DateTime dOut = new DateTime(2012, 3, 7, 14, 59, 59, 0);
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        assertEquals(true, runtime.getRunTimePeriod().isInPeriod(dIn));
        assertEquals(false, runtime.getRunTimePeriod().isInPeriod(dOut));
        assertEquals(false, runtime.hasSubsequentRunTimes());
    }

    @Test
    public final void testWeekkdays() {
        xml =
                "<run_time>" + "<weekdays>" + "<day day=\"1 2 3 4 5 6\">" + "<period end=\"24:00\" begin=\"00:00\"/>" + "</day>" + "</weekdays>"
                        + "</run_time>";
        JSObjRunTime runtime = new JSObjRunTime(factoryWithDefaultPeriod, xml);
        List<DateTime> result = runtime.getDtSingleStarts(CURRENT_WEEK);
        int weekday = DateTimeConstants.MONDAY;
        for (DateTime d : result) {
            assertEquals(weekday, d.getDayOfWeek());
            weekday++;
        }
    }

    @Test
    public final void testWeekkdays2() {
        xml = "<run_time>" + "<weekdays>" + "<day day=\"5\">" + "</day>" + "</weekdays>" + "</run_time>";
        JSObjRunTime runtime = new JSObjRunTime(factoryWithDefaultPeriod, xml);
        List<DateTime> result = runtime.getDtSingleStarts(CURRENT_WEEK);
        assertEquals(1, result.size());
        assertEquals(DateTimeConstants.FRIDAY, result.get(0).getDayOfWeek());
    }

    @Test
    public final void testGetBeginAndEnd() {
        xml = "<run_time begin='15:00' end='17:00' />";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        assertEquals("15:00:00", runtime.getBegin());
        assertEquals("17:00:00", runtime.getEnd());
        xml = "<run_time begin='15:00:50' end='17:00:12' />";
        runtime = new JSObjRunTime(factory, xml);
        assertEquals("15:00:50", runtime.getBegin());
        assertEquals("17:00:12", runtime.getEnd());
    }

    @Test
    public final void testSingleStarts() {
        xml = "<run_time>" + "<period single_start=\"21:00\" />" + "<period single_start=\"22:00\" />" + "</run_time>";
        JSObjRunTime runtime = new JSObjRunTime(factoryWithDefaultPeriod, xml);
        List<DateTime> result = runtime.getDtSingleStarts(NEXT_24H);
        assertEquals("21:00:00", FMT_TIME.print(result.get(0)));
        assertEquals("22:00:00", FMT_TIME.print(result.get(1)));
    }

    @Test
    public final void testDay() {
        DateTime baseDate = new DateTime();
        xml =
                "<run_time>" + "<weekdays>" + "<day day=\"" + baseDate.getDayOfWeek() + "\">" + "<period single_start=\"15:00\" />"
                        + "<period single_start=\"19:00\" />" + "<period single_start=\"11:00\" />" + "</day>" + "</weekdays>" + "</run_time>";
        JSObjRunTime runtime = new JSObjRunTime(factory, xml);
        List<DateTime> result = runtime.getDtSingleStarts(CURRENT_WEEK);
        String datePrefix = FMT_DATE.print(new DateTime()) + " ";
        assertEquals(3, result.size());
        assertEquals(datePrefix + "11:00", FMT_DATE_TIME.print(result.get(0)));
        assertEquals(datePrefix + "15:00", FMT_DATE_TIME.print(result.get(1)));
        assertEquals(datePrefix + "19:00", FMT_DATE_TIME.print(result.get(2)));
    }

    @Test
    public final void testAll() {
        xml =
                "<run_time>" + "<weekdays>" + "<day day=\"2\"/>" + "</weekdays>" + "<ultimos>" + "<day day=\"10\"/>" + "<day day=\"0\"/>"
                        + "</ultimos>" + "<monthdays>" + "<day day=\"22\"/>" + "<weekday day=\"friday\" which=\"4\"/>" + "</monthdays>"
                        + "<date date=\"2012-03-28\"/>" + "</run_time>";
        testRunTime4All(factoryWithDefaultPeriod, xml, "23:59");
    }

    @Test
    public final void testAllWithDefaultPeriod() {
        final String defaultPeriod = "<period end=\"24:00\" begin=\"00:00\"/>";
        xml =
                "<run_time>" + "<weekdays>" + "<day day=\"2\">" + defaultPeriod + "</day>" + "</weekdays>" + "<ultimos>" + "<day day=\"10\">"
                        + defaultPeriod + "</day>" + "<day day=\"0\">" + defaultPeriod + "</day>" + "</ultimos>" + "<monthdays>" + "<day day=\"22\">"
                        + defaultPeriod + "</day>" + "<weekday day=\"friday\" which=\"4\">" + defaultPeriod + "</weekday>" + "</monthdays>"
                        + "<date date=\"2012-03-28\">" + defaultPeriod + "</date>" + "</run_time>";
        testRunTime4All(factoryWithDefaultPeriod, xml, "23:59");
    }

    @Test
    public final void testAllWithSingleStart() {
        final String defaultPeriod = "<period single_start=\"10:02\"/>";
        xml =
                "<run_time>" + "<weekdays>" + "<day day=\"2\">" + defaultPeriod + "</day>" + "</weekdays>" + "<ultimos>" + "<day day=\"10\">"
                        + defaultPeriod + "</day>" + "<day day=\"0\">" + defaultPeriod + "</day>" + "</ultimos>" + "<monthdays>" + "<day day=\"22\">"
                        + defaultPeriod + "</day>" + "<weekday day=\"friday\" which=\"4\">" + defaultPeriod + "</weekday>" + "</monthdays>"
                        + "<date date=\"2012-03-28\">" + defaultPeriod + "</date>" + "</run_time>";
        testRunTime4All(factory, xml, "10:02");
    }

    private void testRunTime4All(SchedulerObjectFactory testFactory, String runTimeXml, String expectedTime) {
        DateTime from = new DateTime(2012, 3, 1, 0, 0, 0, 0);
        Interval march2012 = new Interval(from, from.plusMonths(1));
        JSObjRunTime runtime = new JSObjRunTime(testFactory, runTimeXml);
        List<DateTime> result = runtime.getDtSingleStarts(march2012);
        String timeString = " " + expectedTime;
        assertEquals(9, result.size());
        assertEquals("2012-03-06" + timeString, FMT_DATE_TIME.print(result.get(0)));
        assertEquals("2012-03-13" + timeString, FMT_DATE_TIME.print(result.get(1)));
        assertEquals("2012-03-20" + timeString, FMT_DATE_TIME.print(result.get(2)));
        assertEquals("2012-03-21" + timeString, FMT_DATE_TIME.print(result.get(3)));
        assertEquals("2012-03-22" + timeString, FMT_DATE_TIME.print(result.get(4)));
        assertEquals("2012-03-23" + timeString, FMT_DATE_TIME.print(result.get(5)));
        assertEquals("2012-03-27" + timeString, FMT_DATE_TIME.print(result.get(6)));
        assertEquals("2012-03-28" + timeString, FMT_DATE_TIME.print(result.get(7)));
        assertEquals("2012-03-31" + timeString, FMT_DATE_TIME.print(result.get(8)));
    }

    @Test
    public final void testStartDatesAwareHolidays() {
        final String suppressHoliday = "<period when_holiday=\"suppress\"/>";
        final String ignoreHoliday = "<period when_holiday=\"ignore_holiday\"/>";
        final String previousNonHoliday = "<period when_holiday=\"previous_non_holiday\"/>";
        final String nextNonHoliday = "<period when_holiday=\"next_non_holiday\"/>";
        List<DateTime> result = runStartDatesAwareHolidays(suppressHoliday);
        assertEquals(0, result.size());
        result = runStartDatesAwareHolidays(ignoreHoliday);
        assertEquals(9, result.size());
        result = runStartDatesAwareHolidays(previousNonHoliday);
        assertEquals(4, result.size());
        assertEquals("2012-03-09", FMT_DATE.print(result.get(0)));
        assertEquals("2012-03-16", FMT_DATE.print(result.get(1)));
        assertEquals("2012-03-23", FMT_DATE.print(result.get(2)));
        assertEquals("2012-03-29", FMT_DATE.print(result.get(3)));
        result = runStartDatesAwareHolidays(nextNonHoliday);
        assertEquals(4, result.size());
        assertEquals("2012-03-05", FMT_DATE.print(result.get(0)));
        assertEquals("2012-03-12", FMT_DATE.print(result.get(1)));
        assertEquals("2012-03-19", FMT_DATE.print(result.get(2)));
        assertEquals("2012-03-26", FMT_DATE.print(result.get(3)));
    }

    public List<DateTime> runStartDatesAwareHolidays(String period) {
        DateTime from = new DateTime(2012, 3, 1, 0, 0, 0, 0);
        Interval march2012 = new Interval(from, from.plusMonths(1));
        xml = "<run_time>" + "<weekdays>" + "<day day=\"6\">" + period + "</day>" + "<day day=\"7\">" + period + "</day>" + "</weekdays>"
                        + "<holidays>" + "<holiday date=\"2012-03-01\"/>" + "<holiday date=\"2012-03-02\"/>" + "<holiday date=\"2012-03-30\"/>"
                        + "<weekdays>" + "<day day=\"6\"/>" + "<day day=\"7\"/>" + "</weekdays>" + "</holidays>" + "</run_time>";
        JSObjRunTime runtime = new JSObjRunTime(factoryWithDefaultPeriod, xml);
        return runtime.getDtSingleStarts(march2012);
    }

}