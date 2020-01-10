package com.sos.scheduler.model.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.JodaTools;

/** @author ss */
public class JSObjHolidaysWeekdaysDay extends Holidays.Weekdays.Day {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjWeekdays.class);

    public JSObjHolidaysWeekdaysDay(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public List<DateTime> getDtHolidays(Interval timeRange) {
        List<DateTime> result = new ArrayList<DateTime>();
        List<DateTime> work = getNextSingleStarts(timeRange.getStart());
        for (DateTime date : work) {
            if (timeRange.contains(date)) {
                while (timeRange.contains(date)) {
                    result.add(date);
                    date = date.plusWeeks(1);
                }
            }
        }
        Collections.sort(result, DateTimeComparator.getInstance());
        return result;
    }

    private List<DateTime> getNextSingleStarts(DateTime baseDate) {
        DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter fmtDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        List<DateTime> result = new ArrayList<DateTime>();
        LOGGER.debug(getDay().size() + " day elements detected.");
        Iterator<String> it = getDay().iterator();
        while (it.hasNext()) {
            String dayString = it.next();
            LOGGER.debug("parsing day string " + dayString);
            List<Integer> days = JodaTools.getJodaWeekdays(dayString);
            for (int i = 0; i < days.size(); i++) {
                DateTime nextWeekDay = JodaTools.getNextWeekday(baseDate, days.get(i));
                LOGGER.debug("calculated date " + fmtDate.print(nextWeekDay));
                if (nextWeekDay.isBefore(baseDate)) {
                    nextWeekDay = nextWeekDay.plusWeeks(1);
                    LOGGER.debug("start is corrected to " + fmtDateTime.print(nextWeekDay));
                }
                result.add(nextWeekDay);
            }
            Collections.sort(result, DateTimeComparator.getInstance());
        }
        return result;
    }

}
