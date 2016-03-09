package com.sos.scheduler.model.objects;

import static org.junit.Assert.assertEquals;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

public class IntervalConstantsTest {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(IntervalConstantsTest.class);

    private static final DateTimeFormatter fmtDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public final void testIntervalCurrentDay() {
        Interval i = IntervalConstants.CURRENT_DAY.getInterval();
        DateTime from = i.getStart().minusMillis(i.getStart().getMillisOfDay());
        DateTime to = from.plusDays(1);
        assertEquals(fmtDateTime.print(from), fmtDateTime.print(i.getStart()));
        assertEquals(fmtDateTime.print(to), fmtDateTime.print(i.getEnd()));
    }

    @Test
    public final void testIntervalRestOfDay() {
        Interval i = IntervalConstants.REST_OF_DAY.getInterval();
        DateTime from = i.getStart();
        DateTime to = from.minusMillis(from.getMillisOfDay()).plusDays(1);
        assertEquals(fmtDateTime.print(from), fmtDateTime.print(i.getStart()));
        assertEquals(fmtDateTime.print(to), fmtDateTime.print(i.getEnd()));
    }

    @Test
    public final void testIntervalNext24h() {
        Interval i = IntervalConstants.NEXT_24H.getInterval();
        DateTime from = i.getStart();
        DateTime to = from.plusDays(1);
        assertEquals(fmtDateTime.print(from), fmtDateTime.print(i.getStart()));
        assertEquals(fmtDateTime.print(to), fmtDateTime.print(i.getEnd()));
    }

    @Test
    public final void testIntervalNextWeek() {
        Interval i = IntervalConstants.NEXT_WEEK.getInterval();
        DateTime from = i.getStart();
        DateTime to = from.plusWeeks(1);
        assertEquals(fmtDateTime.print(from), fmtDateTime.print(i.getStart()));
        assertEquals(fmtDateTime.print(to), fmtDateTime.print(i.getEnd()));
    }

}
