package com.sos.scheduler.model.objects;

import java.util.Iterator;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.sos.scheduler.model.ISOSJsObjStartTimes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.RunTimeElement;
import com.sos.scheduler.model.tools.RunTimeElements;

public class JSObjDate extends RunTime.Date implements ISOSJsObjStartTimes {

	public JSObjDate (SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}

	public DateTime getDtDate() {
		DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");
		return fmtDate.parseDateTime(getDate());
	}

	@Override
	public RunTimeElements getRunTimeElements(Interval timeRange) {
		RunTimeElements result = new RunTimeElements(timeRange);
		Iterator<Period> it = getPeriod().iterator();
		while(it.hasNext()) {
			Period p = it.next();
			JSObjPeriod period = new JSObjPeriod(objFactory);
			period.setObjectFieldsFrom(p);
			DateTime singleStart = period.getDtSingleStartOrNull(getDtDate());
			if (singleStart != null && timeRange.contains(singleStart)) {
				result.add( new RunTimeElement(singleStart,period.getWhenHoliday()) );
			}
		}
//		Collections.sort(result, DateTimeComparator.getInstance());
		return result;
	}

	/**
	 * The default behaviour of the JobScheduler Object Model is to provide an empty List of Periods. In some 
	 * cases we want to have exactly one default period.
	 * 
	 * (non-Javadoc)
	 * @see com.sos.scheduler.model.objects.Weekdays.Day#getPeriod()
	 */
	@Override
    public List<Period> getPeriod() {
		List<Period> list = super.getPeriod();
		WhenHoliday h = (list != null && list.size() > 0) ? list.get(0).getWhenHoliday() : WhenHoliday.SUPPRESS;
        return (objFactory.useDefaultPeriod()) ? JSObjRunTime.getDefaultPeriod(objFactory,h) : list;
    }

}
