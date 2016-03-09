package com.sos.scheduler.model.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Interval;

public class RunTimeElements extends TreeMap<DateTime, RunTimeElement> {

    public RunTimeElements(Interval timeRange) {
        super(DateTimeComparator.getInstance());
        this.timeRange = timeRange;
    }

    public RunTimeElements(DateTime baseDate) {
        super(DateTimeComparator.getInstance());
        DateTime from = JodaTools.getStartOfDay(baseDate);
        this.timeRange = new Interval(from, from.plusDays(1));
    }

    private static final long serialVersionUID = -183103162185073046L;

    private final Interval timeRange;

    public void add(RunTimeElement runtime) {
        put(runtime.getStartDate(), runtime);
    }

    public List<DateTime> getStartTimes() {
        List<DateTime> result = new ArrayList<DateTime>();
        for (RunTimeElement e : this.values()) {
            result.add(e.getStartDate());
        }
        return result;
    }

    public Interval getTimeRange() {
        return timeRange;
    }
}
