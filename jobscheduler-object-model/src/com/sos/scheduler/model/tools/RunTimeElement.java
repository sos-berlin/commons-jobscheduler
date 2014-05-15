package com.sos.scheduler.model.tools;

import org.joda.time.DateTime;

import com.sos.scheduler.model.objects.WhenHoliday;

public class RunTimeElement {
	
	private final DateTime startDate;
	private final WhenHoliday whenHoliday;
	
	public RunTimeElement (DateTime startDate, WhenHoliday whenHoliday) {
		this.startDate = startDate;
		this.whenHoliday = whenHoliday;
	}

	public DateTime getStartDate() {
		return startDate;
	}
	
	public WhenHoliday getWhenHoliday() {
		return whenHoliday;
	}

}
