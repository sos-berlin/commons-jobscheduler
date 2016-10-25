package com.sos.scheduler.model.objects;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.ISOSJsObjStartTimes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.RunTimeElement;
import com.sos.scheduler.model.tools.RunTimeElements;

/** @author oh
 * @author ss */
public class JSObjPeriod extends Period implements ISOSJsObjStartTimes {

    public JSObjPeriod(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public JSObjPeriod(SchedulerObjectFactory schedulerObjectFactory, String xmlContent) {
        super();
        objFactory = schedulerObjectFactory;
        Period p = (Period) super.unMarshal(xmlContent);
        setObjectFieldsFrom(p);
    }

    public PeriodType getPeriodType() {
        if (hasSingleStart()) {
            return PeriodType.SINGLE_START;
        }
        if (hasStartStartInterval()) {
            return PeriodType.START_START_INTERVAL;
        }
        if (hasEndStartInterval()) {
            return PeriodType.END_START_INTERVAL;
        }
        throw new JobSchedulerException("the period type of '" + toXMLString() + "' is not valid.");
    }

    public boolean hasSingleStart() {
        return getSingleStart() != null;
    }

    public boolean hasStartStartInterval() {
        return getAbsoluteRepeat() != null;
    }

    public boolean hasEndStartInterval() {
        return getRepeat() != null;
    }

    @Override
    public String getBegin() {
        return JSObjPeriod.normalizeTime(super.getBegin());
    }

    @Override
    public String getEnd() {
        return JSObjPeriod.normalizeTime(super.getEnd());
    }

    @Override
    public String getSingleStart() {
        return JSObjPeriod.normalizeTime(super.getSingleStart());
    }

    public static String normalizeTime(String timeString) {
        return timeString == null || "".equals(timeString) ? null : timeString.length() < 8 ? timeString + ":00" : timeString;
    }

    public boolean isInPeriod(Date timeStamp) {
        DateTime d = new DateTime(timeStamp);
        return isInPeriod(d);
    }

    public boolean isInPeriod(DateTime timeStamp) {
        if (hasSingleStart()) {
            return true;
        }
        Interval p = getPeriodOrNull(timeStamp);
        return p.contains(timeStamp);
    }

    public Interval getPeriodOrNull(DateTime date) {
        Interval result = null;
        if (getBegin() != null && getEnd() != null) {
            DateTime begin = getDate(date, getBegin());
            DateTime end = getDate(date, getEnd());
            result = new Interval(begin, end);
        }
        return result;
    }

    private static DateTime getDate(DateTime baseDate, String timeString) {
        DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter fmtDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        String dateString = fmtDate.print(baseDate) + " ";
        return fmtDateTime.parseDateTime(dateString + timeString);
    }

    public DateTime getDtSingleStartOrNull(DateTime date) {
        DateTime result = null;
        if (hasSingleStart()) {
            result = getDate(date, getSingleStart());
        }
        return result;
    }

    public DateTime getDtBeginOrNull(DateTime date) {
        DateTime result = null;
        if (!hasSingleStart()) {
            result = getDate(date, getBegin());
        }
        return result;
    }

    public DateTime getDtEndOrNull(DateTime date) {
        DateTime result = null;
        if (!hasSingleStart()) {
            result = getDate(date, getEnd());
        }
        return result;
    }

    @Override
    public RunTimeElements getRunTimeElements(Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        DateTime dt = getDtSingleStartOrNull(timeRange.getStart());
        if (dt != null) {
            result = new RunTimeElements(timeRange);
            if (!timeRange.contains(dt)) {
                dt = dt.plusDays(1);
            }
            while (true) {
                if (!timeRange.contains(dt)) {
                    break;
                }
                result.add(new RunTimeElement(dt, getWhenHoliday()));
                dt = dt.plusDays(1);
            }
        }
        return result;
    }

    public DateTime getDtNextSingleStartOrNull() {
        RunTimeElements result = getRunTimeElements(IntervalConstants.NEXT_24H.getInterval());
        return result.isEmpty() ? null : result.getStartTimes().get(0);
    }

}