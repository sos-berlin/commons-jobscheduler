package com.sos.scheduler.model.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.RunTimeElement;
import com.sos.scheduler.model.tools.RunTimeElements;

/** @author oh */
public class JSObjRunTime extends RunTime {

    private final JSObjPeriod period;
    private boolean useDefaultPeriod = false;

    public JSObjRunTime(final SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
        period = getPeriodObject();
    }

    public JSObjRunTime(final SchedulerObjectFactory schedulerObjectFactory, final String xmlContent) {
        super();
        objFactory = schedulerObjectFactory;
        objJAXBElement = (JAXBElement<JSObjBase>) unMarshal(xmlContent);
        setObjectFieldsFrom(objJAXBElement.getValue());
        period = getPeriodObject();
    }

    public JSObjRunTime(final SchedulerObjectFactory schedulerObjectFactory, final RunTime pobjRunTime) {
        super();
        objFactory = schedulerObjectFactory;
        setObjectFieldsFrom(pobjRunTime);
        period = getPeriodObject();
    }

    private JSObjPeriod getPeriodObject() {
        JSObjPeriod period = new JSObjPeriod(objFactory);
        period.setBegin(getBegin());
        period.setEnd(getEnd());
        period.setRepeat(getRepeat());
        period.setLetRun(getLetRun());
        period.setSingleStart(getSingleStart());
        period.setWhenHoliday(getWhenHoliday());
        return period;
    }

    public JSObjPeriod getRunTimePeriod() {
        return period;
    }

    @Override
    public String getBegin() {
        return JSObjPeriod.normalizeTime(super.getBegin());
    }

    @Override
    public String getEnd() {
        return JSObjPeriod.normalizeTime(super.getEnd());
    }

    public boolean hasPeriod() {
        return !getPeriod().isEmpty();
    }

    public boolean hasAt() {
        return !getAt().isEmpty();
    }

    public boolean hasDate() {
        return !getDate().isEmpty();
    }

    public boolean hasWeekdays() {
        return getWeekdays() != null;
    }

    public boolean hasMonth() {
        return !getMonth().isEmpty();
    }

    public boolean hasMonthdays() {
        return getMonthdays() != null && !getMonthdays().getDayOrWeekday().isEmpty();
    }

    public boolean hasUltimos() {
        return getUltimos() != null;
    }

    public boolean hasHolidays() {
        return getHolidays() != null;
    }

    public boolean hasSubsequentRunTimes() {
        return hasPeriod() || hasAt() || hasDate() || hasWeekdays() || hasMonth() || hasMonthdays() || hasUltimos();
    }

    public List<DateTime> getDtSingleStarts(final Interval timeRange) {
        Interval extendedTimeRange = new Interval(timeRange.getStart().minusDays(1), timeRange.getEnd().plusDays(1));
        RunTimeElements runTimes = new RunTimeElements(timeRange);
        runTimes.putAll(getDtWeekdays(extendedTimeRange));
        runTimes.putAll(getDtPeriod(extendedTimeRange));
        runTimes.putAll(getDtAt(extendedTimeRange));
        runTimes.putAll(getDtDate(extendedTimeRange));
        runTimes.putAll(getDtMonthdays(extendedTimeRange));
        runTimes.putAll(getDtUltimos(extendedTimeRange));
        if (runTimes.isEmpty()) {
            RunTimeElements periodStartTimes = period.getRunTimeElements(extendedTimeRange);
            runTimes.putAll(periodStartTimes);
        }
        List<DateTime> result = new ArrayList<DateTime>();
        for (DateTime d : getJsObjHolidays().getStartDatesAwareHolidays(runTimes)) {
            if (timeRange.contains(d)) {
                result.add(d);
            }
        }
        return result;
    }

    public RunTimeElements getDtAt(final Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        Iterator<JSObjAt> it1 = getJsObjAt().iterator();
        while (it1.hasNext()) {
            JSObjAt at = it1.next();
            for (RunTimeElement e : at.getRunTimeElements(timeRange).values()) {
                result.add(e);
            }
        }
        return result;
    }

    public RunTimeElements getDtDate(final Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        Iterator<JSObjDate> it1 = getJsObjDate().iterator();
        while (it1.hasNext()) {
            JSObjDate date = it1.next();
            for (RunTimeElement e : date.getRunTimeElements(timeRange).values()) {
                result.add(e);
            }
        }
        return result;
    }

    public RunTimeElements getDtMonthdays(final Interval timeRange) {
        return getJsObjMonthdays().getRunTimeElements(timeRange);
    }

    public RunTimeElements getDtUltimos(final Interval timeRange) {
        return getJsObjUltimos().getRunTimeElements(timeRange);
    }

    public RunTimeElements getDtPeriod(final Interval timeRange) {
        RunTimeElements result = new RunTimeElements(timeRange);
        Iterator<JSObjPeriod> it1 = getJsObjPeriod().iterator();
        while (it1.hasNext()) {
            JSObjPeriod period = it1.next();
            for (RunTimeElement e : period.getRunTimeElements(timeRange).values()) {
                result.add(e);
            }
        }
        return result;
    }

    public RunTimeElements getDtWeekdays(final Interval timeRange) {
        return getJsObjWeekdays().getRunTimeElements(timeRange);
    }

    public JSObjWeekdays getJsObjWeekdays() {
        JSObjWeekdays weekdays = new JSObjWeekdays(objFactory);
        if (hasWeekdays()) {
            weekdays.setObjectFieldsFrom(getWeekdays());
            weekdays.setHotFolderSrc(getHotFolderSrc());
        }
        return weekdays;
    }

    public JSObjMonthdays getJsObjMonthdays() {
        JSObjMonthdays monthdays = new JSObjMonthdays(objFactory);
        if (hasMonthdays()) {
            monthdays.setObjectFieldsFrom(getMonthdays());
            monthdays.setHotFolderSrc(getHotFolderSrc());
        }
        return monthdays;
    }

    public JSObjUltimos getJsObjUltimos() {
        JSObjUltimos ultimos = new JSObjUltimos(objFactory);
        if (hasUltimos()) {
            ultimos.setObjectFieldsFrom(getUltimos());
            ultimos.setHotFolderSrc(getHotFolderSrc());
        }
        return ultimos;
    }

    public List<JSObjDate> getJsObjDate() {
        List<JSObjDate> result = new ArrayList<JSObjDate>();
        if (hasDate()) {
            Iterator<RunTime.Date> it = getDate().iterator();
            while (it.hasNext()) {
                RunTime.Date d = it.next();
                JSObjDate date = new JSObjDate(objFactory);
                date.setObjectFieldsFrom(d);
                date.setHotFolderSrc(getHotFolderSrc());
                result.add(date);
            }
        }
        return result;
    }

    public List<JSObjAt> getJsObjAt() {
        List<JSObjAt> result = new ArrayList<JSObjAt>();
        if (hasAt()) {
            Iterator<RunTime.At> it = getAt().iterator();
            while (it.hasNext()) {
                RunTime.At d = it.next();
                JSObjAt date = new JSObjAt(objFactory);
                date.setObjectFieldsFrom(d);
                date.setHotFolderSrc(getHotFolderSrc());
                result.add(date);
            }
        }
        return result;
    }

    public JSObjHolidays getJsObjHolidays() {
        return getJsObjHolidays(true);
    }

    public JSObjHolidays getJsObjHolidays(final boolean resolveIncludes) {
        JSObjHolidays result = new JSObjHolidays(objFactory);
        if (hasHolidays()) {
            result.setObjectFieldsFrom(getHolidays());
            result.setHotFolderSrc(getHotFolderSrc());
            if (resolveIncludes) {
                result.resolveIncludes();
            }
        }
        return result;
    }

    public List<JSObjPeriod> getJsObjPeriod() {
        List<JSObjPeriod> result = new ArrayList<JSObjPeriod>();
        if (hasPeriod()) {
            Iterator<Period> it = getPeriod().iterator();
            while (it.hasNext()) {
                Period p = it.next();
                JSObjPeriod period = new JSObjPeriod(objFactory);
                period.setObjectFieldsFrom(p);
                period.setHotFolderSrc(getHotFolderSrc());
                result.add(period);
            }
        }
        return result;
    }

    public static List<Period> getDefaultPeriod(final SchedulerObjectFactory factory, final WhenHoliday h) {
        List<Period> result = new ArrayList<Period>();
        Period period = factory.createPeriod();
        period.setBegin(null);
        period.setEnd(null);
        period.setRepeat(null);
        period.setLetRun(conNO);
        period.setSingleStart("23:59:59");
        period.setWhenHoliday(h);
        result.add(period);
        return result;
    }

    public boolean useDefaultPeriod() {
        return useDefaultPeriod;
    }

    protected void setUseDefaultPeriod(final boolean useDefaultPeriod) {
        this.useDefaultPeriod = useDefaultPeriod;
    }

    @Override
    public Monthdays getMonthdays() {
        Monthdays objM = super.getMonthdays();
        if (objM == null) {
            super.setMonthdays(new Monthdays());
        }
        return super.getMonthdays();
    }

    public boolean hasSingleStart() {
        return !this.getSingleStart().isEmpty();
    }

    public void setSingleStart(final boolean pflgValue) {
        if (pflgValue) {
            this.setSingleStart(conYES);
        } else {
            this.setSingleStart(conEMPTY);
        }
    }

    public void setOnce(final boolean pflgValue) {
        if (pflgValue) {
            this.setOnce(conYES);
        } else {
            this.setOnce(conNO);
        }
    }

    public boolean isOnce() {
        return this.getOnce().equalsIgnoreCase(conYES);
    }

    public boolean isLetRun() {
        return this.getLetRun().equalsIgnoreCase(conYES);
    }

    public void setLetRun(final boolean pflgValue) {
        if (pflgValue) {
            this.setLetRun(conYES);
        } else {
            this.setLetRun(conNO);
        }
    }

}