package com.sos.scheduler.model.objects;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.scheduler.model.tools.JodaTools;

public class JodaToolsTest {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(JodaToolsTest.class);

    private final static DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");
    // private final static DateTimeFormatter fmtDateTime =
    // DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private final static DateTime baseDate = new DateTime(2012, 3, 12, 0, 0, 0, 0);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public final void testStartOfMonth() {
        DateTime result = JodaTools.getStartOfMonth(baseDate);
        assertEquals("2012-03-01", fmtDate.print(result));
    }

    @Test
    public final void testEndOfMonth() {
        DateTime result = JodaTools.getEndOfMonth(baseDate);
        assertEquals("2012-03-31", fmtDate.print(result));
    }

    @Test
    public final void testGetNextWeekday() {
        DateTime result = JodaTools.getNextWeekday(baseDate, DateTimeConstants.WEDNESDAY);
        assertEquals("2012-03-14", fmtDate.print(result));
    }

    @Test
    public final void testGetPreviousWeekday() {
        DateTime result = JodaTools.getPreviousWeekday(baseDate, DateTimeConstants.WEDNESDAY);
        assertEquals("2012-03-07", fmtDate.print(result));
    }

    @Test
    public final void testGetWeekdayInMonth() {
        DateTime result = JodaTools.getWeekdayInMonth(baseDate, DateTimeConstants.WEDNESDAY, 3);
        assertEquals("2012-03-21", fmtDate.print(result));
        result = JodaTools.getWeekdayInMonth(baseDate, DateTimeConstants.WEDNESDAY, -3);
        assertEquals("2012-03-14", fmtDate.print(result));
    }

    @Test
    public final void testGetWeekdayInIntervalOrNull() {
        Interval baseInterval = new Interval(baseDate, baseDate.plusDays(31));
        DateTime result = JodaTools.getWeekdayInIntervalOrNull(baseInterval, DateTimeConstants.WEDNESDAY, 1);
        assertEquals("2012-04-04", fmtDate.print(result));

        baseInterval = new Interval(baseDate, baseDate.plusDays(5));
        result = JodaTools.getWeekdayInIntervalOrNull(baseInterval, DateTimeConstants.WEDNESDAY, 1);
        assertEquals(null, result);
    }

    @Test
    public final void testGetDayInMonth() {
        DateTime result = JodaTools.getDayInMonth(baseDate, 19);
        assertEquals("2012-03-19", fmtDate.print(result));
        result = JodaTools.getDayInMonth(baseDate, -3);
        assertEquals("2012-03-28", fmtDate.print(result));
        result = JodaTools.getDayInMonth(baseDate, 0);
        assertEquals("2012-03-31", fmtDate.print(result));
        result = JodaTools.getDayInMonth(baseDate, -1);
        assertEquals("2012-03-30", fmtDate.print(result));
    }

    @Test
    public final void testGetMonthWeekdayOrNull() {
        Interval baseInterval = new Interval(baseDate, baseDate.plusDays(31));
        DateTime result = JodaTools.getDayInIntervalOrNull(baseInterval, 12);
        assertEquals("2012-03-12", fmtDate.print(result));

        baseInterval = new Interval(baseDate, baseDate.plusDays(5));
        result = JodaTools.getDayInIntervalOrNull(baseInterval, -5);
        assertEquals(null, result);
    }

    @Test
    public final void testGetJodayWeekday() {
        String wednesday = "3";
        int jodaDay = JodaTools.getJodaWeekday(wednesday);
        assertEquals(DateTimeConstants.WEDNESDAY, jodaDay);
    }

    @Test
    public final void testGetJodayWeekdays() {
        String jsDays = "1 3 5";
        List<Integer> days = JodaTools.getJodaWeekdays(jsDays);
        assertEquals(DateTimeConstants.MONDAY, days.get(0).intValue());
        assertEquals(DateTimeConstants.WEDNESDAY, days.get(1).intValue());
        assertEquals(DateTimeConstants.FRIDAY, days.get(2).intValue());
        jsDays = "di do so";
        days = JodaTools.getJodaWeekdays(jsDays);
        assertEquals(DateTimeConstants.TUESDAY, days.get(0).intValue());
        assertEquals(DateTimeConstants.THURSDAY, days.get(1).intValue());
        assertEquals(DateTimeConstants.SUNDAY, days.get(2).intValue());
    }

}
