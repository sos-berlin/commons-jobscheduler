package com.sos.scheduler.model.objects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.DataElements.JSDataElementDate;
import com.sos.JSHelper.DataElements.JSDateFormat;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.Holidays.Weekdays.Day;
import com.sos.scheduler.model.tools.RunTimeElement;
import com.sos.scheduler.model.tools.RunTimeElements;

/** @author oh */
public class JSObjHolidays extends Holidays {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjHolidays.class);
    private List<JSObjInclude> includes = null;

    public JSObjHolidays(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public JSObjHolidays(SchedulerObjectFactory schedulerObjectFactory, ISOSVirtualFile pobjVirtualFile) {
        this(schedulerObjectFactory);
        Holidays objHolidays = (Holidays) unMarshal(pobjVirtualFile);
        setObjectFieldsFrom(objHolidays);
        setHotFolderSrc(pobjVirtualFile);
    }

    public boolean isAHoliday(final Calendar pobjCalendar) {
        return isAHoliday(pobjCalendar.getTime());
    }

    public boolean isAHoliday(Date pobjDate) {
        boolean flgIsAHoliday = false;
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTime(pobjDate);
        int intDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        intDayOfWeek -= 1;
        if (intDayOfWeek == 0) {
            intDayOfWeek = 7;
        }
        for (Object objO : this.getWeekdaysOrHolidayOrInclude()) {
            if (objO instanceof Weekdays) {
                Weekdays objW = (Weekdays) objO;
                for (Day objDay : objW.day) {
                    for (String strD : objDay.day) {
                        if (intDayOfWeek == new Integer(strD)) {
                            flgIsAHoliday = true;
                            return flgIsAHoliday;
                        }
                    }
                }
            }
        }
        for (Object objO : this.getWeekdaysOrHolidayOrInclude()) {
            if (objO instanceof Holiday) {
                Holiday objH = (Holiday) objO;
                String strD = objH.getDate();
                Date objD = new JSDataElementDate(strD, JSDateFormat.dfDATE_SHORT).getDateObject();
                if (objD.equals(pobjDate)) {
                    flgIsAHoliday = true;
                    return flgIsAHoliday;
                }
            }
        }
        return flgIsAHoliday;
    }

    public boolean isHoliday(DateTime date) {
        DateTime from = JodaTools.getStartOfDay(date);
        List<DateTime> result = getHolidays(new Interval(from, from.plusDays(1)));
        return !result.isEmpty();
    }

    public List<DateTime> getHolidays(Interval timeRange) {
        List<DateTime> result = new ArrayList<DateTime>();
        result.addAll(getDtHoliday(timeRange));
        result.addAll(getDtWeekdays(timeRange));
        result.addAll(getDtInclude(timeRange));
        return result;
    }

    public DateTime getNextNonHoliday(DateTime date) {
        DateTime result = date;
        while (isHoliday(result)) {
            result = result.plusDays(1);
        }
        return result;
    }

    public DateTime getPreviousNonHoliday(DateTime date) {
        DateTime result = date;
        while (isHoliday(result)) {
            result = result.minusDays(1);
        }
        return result;
    }

    private List<DateTime> getDtWeekdays(Interval timeRange) {
        List<DateTime> result = new ArrayList<DateTime>();
        Iterator<Object> it = getWeekdaysOrHolidayOrInclude().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Weekdays) {
                Weekdays w = (Weekdays) o;
                JSObjHolidaysWeekdays weekdays = new JSObjHolidaysWeekdays(objFactory);
                weekdays.setObjectFieldsFrom(w);
                result.addAll(weekdays.getDtHolidays(timeRange));
            }
        }
        return result;
    }

    private List<DateTime> getDtHoliday(Interval timeRange) {
        List<DateTime> result = new ArrayList<DateTime>();
        Iterator<Object> it = getWeekdaysOrHolidayOrInclude().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Holiday) {
                Holiday h = (Holiday) o;
                JSObjHoliday holiday = new JSObjHoliday(objFactory);
                holiday.setObjectFieldsFrom(h);
                DateTime d = holiday.getDtHoliday();
                if (timeRange.contains(d)) {
                    result.add(d);
                }
            }
        }
        return result;
    }

    private List<DateTime> getDtInclude(Interval timeRange) {
        List<DateTime> result = new ArrayList<DateTime>();
        Iterator<Object> it = getWeekdaysOrHolidayOrInclude().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Include) {
                Include i = (Include) o;
                LOGGER.warn("the <include> element is not parsed yet.");
                LOGGER.debug(i.getLiveFile());
                LOGGER.debug(i.getHotFolderSrc().toString());
                LOGGER.debug(getHotFolderSrc().toString());
            }
        }
        return result;
    }

    public List<DateTime> getStartDatesAwareHolidays(RunTimeElements runTimes) {
        List<DateTime> result = new ArrayList<DateTime>();
        Interval timeRange = runTimes.getTimeRange();
        if (getHolidays(timeRange).isEmpty()) {
            return runTimes.getStartTimes();
        }
        for (RunTimeElement runTime : runTimes.values()) {
            if (isHoliday(runTime.getStartDate())) {
                switch (runTime.getWhenHoliday()) {
                case SUPPRESS:
                    break;
                case IGNORE_HOLIDAY:
                    if (!result.contains(runTime.getStartDate())) {
                        result.add(runTime.getStartDate());
                    }
                    break;
                case NEXT_NON_HOLIDAY:
                    DateTime nextStart = getNextNonHoliday(runTime.getStartDate());
                    if (timeRange.contains(nextStart) && !result.contains(nextStart)) {
                        result.add(nextStart);
                    }
                    break;
                case PREVIOUS_NON_HOLIDAY:
                    DateTime previousStart = getPreviousNonHoliday(runTime.getStartDate());
                    if (timeRange.contains(previousStart) && !result.contains(previousStart)) {
                        result.add(previousStart);
                    }
                    break;
                }
            }
        }
        return result;
    }

    public List<JSObjInclude> getJsObjInclude() {
        if (includes == null) {
            includes = new ArrayList<JSObjInclude>();
            Iterator<Object> it = getWeekdaysOrHolidayOrInclude().iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof Include) {
                    Include i = (Include) o;
                    JSObjInclude include = new JSObjInclude(objFactory);
                    include.setObjectFieldsFrom(i);
                    include.setHotFolderSrc(getHotFolderSrc());
                    includes.add(include);
                }
            }
        }
        return includes;
    }

    public void resolveIncludes() {
        List<JSObjInclude> list = getJsObjInclude();
        for (JSObjInclude include : list) {
            JSObjHolidays includeHolidays = new JSObjHolidays(objFactory, include.getHotFolderSrc());
            weekdaysOrHolidayOrInclude.addAll(includeHolidays.getWeekdaysOrHolidayOrInclude());
        }
        for (int i = getWeekdaysOrHolidayOrInclude().size() - 1; i >= 0; i--) {
            Object o = getWeekdaysOrHolidayOrInclude().get(i);
            if (o instanceof Include) {
                weekdaysOrHolidayOrInclude.remove(o);
            }
        }
    }

}