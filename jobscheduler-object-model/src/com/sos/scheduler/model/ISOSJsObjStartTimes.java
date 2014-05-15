package com.sos.scheduler.model;

import org.joda.time.Interval;

import com.sos.scheduler.model.tools.RunTimeElements;

public interface ISOSJsObjStartTimes {
	
	
	/**
	 * \brief get all single starts in a given interval
	 * @param timeRange
	 * @return
	 */
	public RunTimeElements getRunTimeElements(Interval timeRange);

}