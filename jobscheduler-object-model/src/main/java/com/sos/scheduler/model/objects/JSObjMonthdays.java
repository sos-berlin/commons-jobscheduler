package com.sos.scheduler.model.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Interval;

import com.sos.scheduler.model.ISOSJsObjStartTimes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.RunTimeElements;

/** @author oh */
public class JSObjMonthdays extends Monthdays implements ISOSJsObjStartTimes {

    public JSObjMonthdays(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    @Override
    public RunTimeElements getRunTimeElements(Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        result.putAll(getDtWeekdays(timeRange));
        result.putAll(getDtDays(timeRange));
        return result;
    }

    public List<JSObjMonthdaysDay> getJsObjMonthdaysDay() {
        List<JSObjMonthdaysDay> result = new ArrayList<JSObjMonthdaysDay>();
        Iterator<Object> it = getDayOrWeekday().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Day) {
                Day d = (Day) o;
                JSObjMonthdaysDay day = new JSObjMonthdaysDay(objFactory);
                day.setObjectFieldsFrom(d);
                day.setHotFolderSrc(getHotFolderSrc());
                result.add(day);
            }
        }
        return result;
    }

    public List<JSObjWeekday> getJsObjWeekday() {
        List<JSObjWeekday> result = new ArrayList<JSObjWeekday>();
        Iterator<Object> it = getDayOrWeekday().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Weekday) {
                Weekday w = (Weekday) o;
                JSObjWeekday weekday = new JSObjWeekday(objFactory);
                weekday.setObjectFieldsFrom(w);
                weekday.setHotFolderSrc(getHotFolderSrc());
                result.add(weekday);
            }
        }
        return result;
    }

    public RunTimeElements getDtWeekdays(Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        for (JSObjWeekday weekday : getJsObjWeekday()) {
            result.putAll(weekday.getRunTimeElements(timeRange));
        }
        return result;
    }

    public RunTimeElements getDtDays(Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        for (JSObjMonthdaysDay day : getJsObjMonthdaysDay()) {
            result.putAll(day.getRunTimeElements(timeRange));
        }
        return result;
    }

}