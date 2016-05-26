package com.sos.scheduler.model.objects;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.sos.scheduler.model.tools.JodaTools;

public class JodaToolsTest {

    private static final DateTimeFormatter FMT_DATE = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTime BASE_DATE = new DateTime(2012, 3, 12, 0, 0, 0, 0);

    @Test
    public final void testStartOfMonth() {
        DateTime result = JodaTools.getStartOfMonth(BASE_DATE);
        assertEquals("2012-03-01", FMT_DATE.print(result));
    }

    @Test
    public final void testEndOfMonth() {
        DateTime result = JodaTools.getEndOfMonth(BASE_DATE);
        assertEquals("2012-03-31", FMT_DATE.print(result));
    }

    @Test
    public final void testGetNextWeekday() {
        DateTime result = JodaTools.getNextWeekday(BASE_DATE, DateTimeConstants.WEDNESDAY);
        assertEquals("2012-03-14", FMT_DATE.print(result));
    }

    @Test
    public final void testGetPreviousWeekday() {
        DateTime result = JodaTools.getPreviousWeekday(BASE_DATE, DateTimeConstants.WEDNESDAY);
        assertEquals("2012-03-07", FMT_DATE.print(result));
    }

    @Test
    public final void testGetWeekdayInMonth() {
        DateTime result = JodaTools.getWeekdayInMonth(BASE_DATE, DateTimeConstants.WEDNESDAY, 3);
        assertEquals("2012-03-21", FMT_DATE.print(result));
        result = JodaTools.getWeekdayInMonth(BASE_DATE, DateTimeConstants.WEDNESDAY, -3);
        assertEquals("2012-03-14", FMT_DATE.print(result));
    }

    @Test
    public final void testGetWeekdayInIntervalOrNull() {
        Interval baseInterval = new Interval(BASE_DATE, BASE_DATE.plusDays(31));
        DateTime result = JodaTools.getWeekdayInIntervalOrNull(baseInterval, DateTimeConstants.WEDNESDAY, 1);
        assertEquals("2012-04-04", FMT_DATE.print(result));
        baseInterval = new Interval(BASE_DATE, BASE_DATE.plusDays(5));
        result = JodaTools.getWeekdayInIntervalOrNull(baseInterval, DateTimeConstants.WEDNESDAY, 1);
        assertEquals(null, result);
    }

    @Test
    public final void testGetDayInMonth() {
        DateTime result = JodaTools.getDayInMonth(BASE_DATE, 19);
        assertEquals("2012-03-19", FMT_DATE.print(result));
        result = JodaTools.getDayInMonth(BASE_DATE, -3);
        assertEquals("2012-03-28", FMT_DATE.print(result));
        result = JodaTools.getDayInMonth(BASE_DATE, 0);
        assertEquals("2012-03-31", FMT_DATE.print(result));
        result = JodaTools.getDayInMonth(BASE_DATE, -1);
        assertEquals("2012-03-30", FMT_DATE.print(result));
    }

    @Test
    public final void testGetMonthWeekdayOrNull() {
        Interval baseInterval = new Interval(BASE_DATE, BASE_DATE.plusDays(31));
        DateTime result = JodaTools.getDayInIntervalOrNull(baseInterval, 12);
        assertEquals("2012-03-12", FMT_DATE.print(result));
        baseInterval = new Interval(BASE_DATE, BASE_DATE.plusDays(5));
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