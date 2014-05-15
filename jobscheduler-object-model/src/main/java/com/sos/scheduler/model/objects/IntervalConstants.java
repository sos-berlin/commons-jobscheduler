package com.sos.scheduler.model.objects;

import org.joda.time.DateTime;
import org.joda.time.Interval;

public enum IntervalConstants {
	CURRENT_DAY, CURRENT_WEEK, REST_OF_DAY, NEXT_24H, NEXT_WEEK;

	public Interval getInterval() {
		DateTime from = new DateTime();
		DateTime to = null;
		switch (this) {
		case CURRENT_DAY:
			from = from.minusMillis(from.getMillisOfDay());
			to = from.plusDays(1);
			break;
		case CURRENT_WEEK:
			from = from.minusMillis(from.getMillisOfDay()).minusDays(from.getDayOfWeek());
			to = from.plusWeeks(1);
			break;
			case REST_OF_DAY:
				to = from.minusMillis(from.getMillisOfDay()).plusDays(1);
				break;
			case NEXT_24H:
				to = from.plusDays(1);
				break;
			case NEXT_WEEK:
				to = from.plusWeeks(1);
				break;
		}
		return new Interval(from, to);
	}

}
