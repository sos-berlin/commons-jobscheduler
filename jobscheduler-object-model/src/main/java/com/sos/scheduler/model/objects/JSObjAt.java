package com.sos.scheduler.model.objects;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.sos.scheduler.model.ISOSJsObjStartTimes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.RunTimeElement;
import com.sos.scheduler.model.tools.RunTimeElements;

public class JSObjAt extends RunTime.At implements ISOSJsObjStartTimes {

	public JSObjAt (SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}

	public DateTime getDtAt() {
		DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");
		return fmtDate.parseDateTime(getAt());
	}

//	@Override
//	public List<DateTime> getDtNextSingleStarts() {
//		List<DateTime> result = new ArrayList<DateTime>();
//		result.add(getDtAt());
//		return result;
//	}

	@Override
	public RunTimeElements getRunTimeElements(Interval timeRange) {
		RunTimeElements result = new RunTimeElements(timeRange);
		if (timeRange.contains(getDtAt())) {
			// The at element does not have a subsequent period element, therefore we use IGNORE_HOLIDAY anyway.
			RunTimeElement e = new RunTimeElement(getDtAt(), WhenHoliday.IGNORE_HOLIDAY);
			result.add(e);
		}
		return result;
	}

}
