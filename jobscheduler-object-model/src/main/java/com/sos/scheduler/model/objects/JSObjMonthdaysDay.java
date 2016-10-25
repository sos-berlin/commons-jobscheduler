package com.sos.scheduler.model.objects;

import java.util.Iterator;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.sos.scheduler.model.ISOSJsObjStartTimes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.JodaTools;
import com.sos.scheduler.model.tools.RunTimeElement;
import com.sos.scheduler.model.tools.RunTimeElements;

/** @author oh */
public class JSObjMonthdaysDay extends Monthdays.Day implements ISOSJsObjStartTimes {

    public JSObjMonthdaysDay(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    @Override
    public RunTimeElements getRunTimeElements(Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        Iterator<Integer> it = getDay().iterator();
        while (it.hasNext()) {
            int day = it.next();
            DateTime date = JodaTools.getDayInIntervalOrNull(timeRange, day);
            while (date != null) {
                Iterator<Period> itP = getPeriod().iterator();
                while (itP.hasNext()) {
                    Period p = itP.next();
                    JSObjPeriod period = new JSObjPeriod(objFactory);
                    period.setObjectFieldsFrom(p);
                    DateTime start = period.getDtSingleStartOrNull(date);
                    if (start != null && timeRange.contains(start)) {
                        result.add(new RunTimeElement(start, period.getWhenHoliday()));
                    }
                }
                DateTime start = JodaTools.getStartOfMonth(date.plusMonths(1));
                if (!timeRange.contains(start)) {
                    break;
                }
                Interval i = new Interval(start, timeRange.getEnd());
                date = JodaTools.getDayInIntervalOrNull(i, day);
            }
        }
        return result;
    }

    @Override
    public List<Period> getPeriod() {
        List<Period> list = super.getPeriod();
        WhenHoliday h = (list != null && !list.isEmpty()) ? list.get(0).getWhenHoliday() : WhenHoliday.SUPPRESS;
        return (objFactory.useDefaultPeriod()) ? JSObjRunTime.getDefaultPeriod(objFactory, h) : list;
    }

}