package com.sos.scheduler.model.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Interval;

import com.sos.scheduler.model.ISOSJsObjStartTimes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.RunTimeElements;

/** @author oh */
public class JSObjWeekdays extends Weekdays implements ISOSJsObjStartTimes {

    public JSObjWeekdays(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    @Override
    public RunTimeElements getRunTimeElements(Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        Iterator<Day> it = getDay().iterator();
        while (it.hasNext()) {
            Day d = it.next();
            JSObjWeekdaysDay day = new JSObjWeekdaysDay(objFactory);
            day.setObjectFieldsFrom(d);
            result.putAll(day.getRunTimeElements(timeRange));
        }
        return result;
    }

    public List<JSObjWeekdaysDay> getJsObjDay() {
        List<JSObjWeekdaysDay> result = new ArrayList<JSObjWeekdaysDay>();
        Iterator<Day> it = getDay().iterator();
        while (it.hasNext()) {
            Day d = it.next();
            JSObjWeekdaysDay day = new JSObjWeekdaysDay(objFactory);
            day.setObjectFieldsFrom(d);
            day.setHotFolderSrc(getHotFolderSrc());
            result.add(day);
        }
        return result;
    }

}