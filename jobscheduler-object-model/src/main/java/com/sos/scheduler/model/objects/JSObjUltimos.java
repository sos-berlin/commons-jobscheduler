package com.sos.scheduler.model.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Interval;

import com.sos.scheduler.model.ISOSJsObjStartTimes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.RunTimeElements;

/** @author oh */
public class JSObjUltimos extends Ultimos implements ISOSJsObjStartTimes {

    public JSObjUltimos(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    @Override
    public RunTimeElements getRunTimeElements(Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        result.putAll(getDtDays(timeRange));
        return result;
    }

    public RunTimeElements getDtDays(Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        for (JSObjUltimosDay day : getJsObjUltimosDay()) {
            result.putAll(day.getRunTimeElements(timeRange));
        }
        return result;
    }

    public List<JSObjUltimosDay> getJsObjUltimosDay() {
        List<JSObjUltimosDay> result = new ArrayList<JSObjUltimosDay>();
        Iterator<Ultimos.Day> it = getDay().iterator();
        while (it.hasNext()) {
            Day d = it.next();
            JSObjUltimosDay day = new JSObjUltimosDay(objFactory);
            day.setObjectFieldsFrom(d);
            day.setHotFolderSrc(getHotFolderSrc());
            result.add(day);
        }
        return result;
    }

}