package com.sos.scheduler.model.objects;

import java.util.Date;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.ISOSJsObjStartTimes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.RunTimeElement;
import com.sos.scheduler.model.tools.RunTimeElements;

/** \file JSObjPeriod.java \brief
 * 
 * \class JSObjPeriod \brief
 * 
 * \details
 *
 * \code \endcode
 *
 * \author oh \version 1.0 - 09.02.2011 15:07:56
 *
 * \author ss \version 1.1 - 03.03.2012 14:02:21 <div class="sos_branding">
 * <p>
 * © 2010 SOS GmbH - Berlin (<a style='color:silver'
 * href='http://www.sos-berlin.com'>http://www.sos-berlin.com</a>)
 * </p>
 * </div> */
public class JSObjPeriod extends Period implements ISOSJsObjStartTimes {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSObjPeriod.class);

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
        if (hasSingleStart())
            return PeriodType.SINGLE_START;
        if (hasStartStartInterval())
            return PeriodType.START_START_INTERVAL;
        if (hasEndStartInterval())
            return PeriodType.END_START_INTERVAL;
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

    /*
     * (non-Javadoc)
     * @see com.sos.scheduler.model.objects.ISOSJsObjPeriod#getBegin()
     */
    @Override
    public String getBegin() {
        return JSObjPeriod.normalizeTime(super.getBegin());
    }

    /*
     * (non-Javadoc)
     * @see com.sos.scheduler.model.objects.ISOSJsObjPeriod#getEnd()
     */
    @Override
    public String getEnd() {
        return JSObjPeriod.normalizeTime(super.getEnd());
    }

    /*
     * (non-Javadoc)
     * @see com.sos.scheduler.model.objects.ISOSJsObjPeriod#getSingleStart()
     */
    @Override
    public String getSingleStart() {
        return JSObjPeriod.normalizeTime(super.getSingleStart());
    }

    public static String normalizeTime(String timeString) {
        return (timeString == null || timeString.equals("")) ? null : (timeString.length() < 8) ? timeString + ":00" : timeString;
    }

    public boolean isInPeriod(Date timeStamp) {
        DateTime d = new DateTime(timeStamp);
        return isInPeriod(d);
    }

    public boolean isInPeriod(DateTime timeStamp) {
        if (hasSingleStart())
            return true;
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

    /** \brief get the single start as a DateTime object \detail To calculate the
     * starttime it is necessary to call this method with a base date.
     * 
     * @param date
     * @return */
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
            if (!timeRange.contains(dt))
                dt = dt.plusDays(1);
            while (true) {
                if (!timeRange.contains(dt))
                    break;
                result.add(new RunTimeElement(dt, getWhenHoliday()));
                dt = dt.plusDays(1);
            }
            // Collections.sort(result, DateTimeComparator.getInstance());
        }
        return result;
    }

    public DateTime getDtNextSingleStartOrNull() {
        RunTimeElements result = getRunTimeElements(IntervalConstants.NEXT_24H.getInterval());
        return (result.size() == 0) ? null : result.getStartTimes().get(0);
    }

}
