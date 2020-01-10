package com.sos.scheduler.model.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Interval;

import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjHolidaysWeekdays extends Holidays.Weekdays {

    public JSObjHolidaysWeekdays(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public List<DateTime> getDtHolidays(Interval timeRange) {
        List<DateTime> result = new ArrayList<DateTime>();
        Iterator<Day> it = getDay().iterator();
        while (it.hasNext()) {
            Day d = it.next();
            JSObjHolidaysWeekdaysDay day = new JSObjHolidaysWeekdaysDay(objFactory);
            day.setObjectFieldsFrom(d);
            result.addAll(day.getDtHolidays(timeRange));
        }
        Collections.sort(result, DateTimeComparator.getInstance());
        return result;
    }

    public List<JSObjWeekdaysDay> getJsObjDay() {
        List<JSObjWeekdaysDay> result = new ArrayList<JSObjWeekdaysDay>();
        Iterator<Day> it = getDay().iterator();
        while (it.hasNext()) {
            Day d = it.next();
            JSObjWeekdaysDay day = new JSObjWeekdaysDay(objFactory);
            day.setObjectFieldsFrom(d);
            result.add(day);
        }
        return result;
    }

}
