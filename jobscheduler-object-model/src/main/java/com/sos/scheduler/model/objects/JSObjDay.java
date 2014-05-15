package com.sos.scheduler.model.objects;

import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.sos.scheduler.model.ISOSJsObjStartTimes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.JodaTools;
import com.sos.scheduler.model.tools.RunTimeElement;
import com.sos.scheduler.model.tools.RunTimeElements;

/**
 * \file JSObjDay.java
 * \brief runtime defintions for specific weekdays
 *  
 * \class JSObjDay
 * \brief runtime defintions for specific weekdays
 * 
 * \details
 *
 * \code
  \endcode
 *
 * \author ss
 * \version 1.0 - 14.03.2012 08:25:39
 * <div class="sos_branding">
 *   <p>© 2010 SOS GmbH - Berlin (<a style='color:silver' href='http://www.sos-berlin.com'>http://www.sos-berlin.com</a>)</p>
 * </div>
 */
public class JSObjDay extends Weekdays.Day implements ISOSJsObjStartTimes {
	
	private static final Logger logger = Logger.getLogger(JSObjDay.class);

	public JSObjDay (SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}

	@Override
	public RunTimeElements getRunTimeElements(Interval timeRange) {
		RunTimeElements result = new RunTimeElements(timeRange);
		RunTimeElements work = getNextSingleStarts(timeRange.getStart());
		for(RunTimeElement runtime : work.values()) {
			DateTime date = runtime.getStartDate();
			if(timeRange.contains(date)) {
				while(timeRange.contains(date)) {
					result.add( new RunTimeElement(date,runtime.getWhenHoliday()) );
					date = date.plusWeeks(1);
				}
			}
		}
//		Collections.sort(result, DateTimeComparator.getInstance());
		return result;
	}

	private RunTimeElements getNextSingleStarts(DateTime baseDate) {
		DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");
		DateTimeFormatter fmtDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		RunTimeElements result = new RunTimeElements(baseDate);
		logger.debug(getDay().size() + " day elements detected.");
		Iterator<String> it = getDay().iterator();
		while(it.hasNext()) {
			String dayString = it.next();
			logger.debug("parsing day string " + dayString);
			List<Integer> days = JodaTools.getJodaWeekdays(dayString);
			for (int i = 0; i < days.size(); i++) {
				DateTime nextWeekDay = JodaTools.getNextWeekday(baseDate, days.get(i));
				logger.debug("calculated date " + fmtDate.print(nextWeekDay));
				List<Period> periods = getPeriod();
				Iterator<Period> itP = periods.iterator();
				logger.debug(periods.size() + " periods found.");
				while (itP.hasNext()) {
					Period p = itP.next();
					JSObjPeriod period = new JSObjPeriod(objFactory);
					period.setObjectFieldsFrom(p);
					DateTime start = period.getDtSingleStartOrNull(nextWeekDay);
					if (start != null) {
						logger.debug("start from period " + fmtDateTime.print(start));
						if (start.isBefore(baseDate)) {
							start = start.plusWeeks(1);
							logger.debug("start is corrected to " + fmtDateTime.print(start));
						}
						RunTimeElement e = new RunTimeElement(start,period.getWhenHoliday());
						result.add(e);
					}
				}
			}
//			Collections.sort(result, DateTimeComparator.getInstance());
		}
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
