package com.sos.scheduler.model.objects;

import java.util.Iterator;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.scheduler.model.ISOSJsObjStartTimes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.JodaTools;
import com.sos.scheduler.model.tools.RunTimeElement;
import com.sos.scheduler.model.tools.RunTimeElements;

/** @author oh */
public class JSObjWeekdaysDay extends Weekdays.Day implements ISOSJsObjStartTimes {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjWeekdays.class);

    public JSObjWeekdaysDay(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    @Override
    public RunTimeElements getRunTimeElements(Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        RunTimeElements work = getNextSingleStarts(timeRange.getStart());
        for (RunTimeElement runtime : work.values()) {
            DateTime date = runtime.getStartDate();
            if (timeRange.contains(date)) {
                while (timeRange.contains(date)) {
                    result.add(new RunTimeElement(date, runtime.getWhenHoliday()));
                    date = date.plusWeeks(1);
                }
            }
        }
        return result;
    }

    private RunTimeElements getNextSingleStarts(DateTime baseDate) {
        DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter fmtDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        RunTimeElements result = new RunTimeElements(baseDate);
        LOGGER.debug(getDay().size() + " day elements detected.");
        Iterator<String> it = getDay().iterator();
        while (it.hasNext()) {
            String dayString = it.next();
            LOGGER.debug("parsing day string " + dayString);
            List<Integer> days = JodaTools.getJodaWeekdays(dayString);
            for (int i = 0; i < days.size(); i++) {
                DateTime nextWeekDay = JodaTools.getNextWeekday(baseDate, days.get(i));
                LOGGER.debug("calculated date " + fmtDate.print(nextWeekDay));
                List<Period> periods = getPeriod();
                Iterator<Period> itP = periods.iterator();
                LOGGER.debug(periods.size() + " periods found.");
                while (itP.hasNext()) {
                    Period p = itP.next();
                    JSObjPeriod period = new JSObjPeriod(objFactory);
                    period.setObjectFieldsFrom(p);
                    DateTime start = period.getDtSingleStartOrNull(nextWeekDay);
                    if (start != null) {
                        LOGGER.debug("start from period " + fmtDateTime.print(start));
                        if (start.isBefore(baseDate)) {
                            start = start.plusWeeks(1);
                            LOGGER.debug("start is corrected to " + fmtDateTime.print(start));
                        }
                        result.add(new RunTimeElement(start, period.getWhenHoliday()));
                    }
                }
            }
        }
        return result;
    }

    /** The default behaviour of the JobScheduler Object Model is to provide an
     * empty List of Periods. In some cases we want to have exactly one default
     * period.
     * 
     * (non-Javadoc)
     * 
     * @see com.sos.scheduler.model.objects.Weekdays.Day#getPeriod() */
    @Override
    public List<Period> getPeriod() {
        List<Period> list = super.getPeriod();
        WhenHoliday h = (list != null && !list.isEmpty()) ? list.get(0).getWhenHoliday() : WhenHoliday.SUPPRESS;
        return objFactory.useDefaultPeriod() ? JSObjRunTime.getDefaultPeriod(objFactory, h) : list;
    }

}
