package com.sos.scheduler.model.objects;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.TestBase;
import com.sos.scheduler.model.objects.Weekdays.Day;

public class JSObjWeekdaysTest extends TestBase {

	private final static Logger logger = Logger.getLogger(JSObjWeekdaysTest.class);

	private final static DateTimeFormatter fmtDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	private final static DateTimeFormatter fmtTime = DateTimeFormat.forPattern("HH:mm:ss");

	@SuppressWarnings("unused")
	private final static Interval next24H = IntervalConstants.NEXT_24H.getInterval();
	private final static Interval nextWeek = IntervalConstants.NEXT_WEEK.getInterval();

	private static SchedulerObjectFactory factory = null;
	private static SchedulerObjectFactory factoryDefaultPeriods = null;

	private String xml;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		factory = new SchedulerObjectFactory();
		factory.initMarshaller(Spooler.class);
		factoryDefaultPeriods = new SchedulerObjectFactory();
		factoryDefaultPeriods.setUseDefaultPeriod(true);
		factoryDefaultPeriods.initMarshaller(Spooler.class);
	}

	@Test
	public final void testSetValidXmlContent() {
		String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
		xml = "<run_time xmlns:ns2=\"job-chain-extensions\">\n"
				+ "    <weekdays>\n"
				+ "        <day day=\"1\">\n"
				+ "            <period single_start=\"15:00\"/>\n"
				+ "        </day>\n"
				+ "    </weekdays>\n"
				+ "</run_time>\n";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);
		assertEquals(xmlHeader + xml, runtime.toXMLString());
	}

	@Test(expected= JobSchedulerException.class)
	public final void testSetInvalidXmlContent() {
		xml = "<invalid weekday />";
		JSObjPeriod period = new JSObjPeriod(factory,xml);
		logger.debug(period.toXMLString());
	}

	@Test
	public final void testSingleDay() {
		xml = "<run_time>" +
		"<weekdays>" +
		"<day day=\"1\">" +
		"<period single_start=\"15:00\" />" +
		"<period single_start=\"19:00:05\" />" +
		"<period single_start=\"11:00\" />" +
		"</day>" +
		"</weekdays>" +
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);

		Iterator<Day> itD = runtime.getWeekdays().getDay().iterator();
		while(itD.hasNext()) {
			Day d = itD.next();
			JSObjWeekdaysDay date = new JSObjWeekdaysDay(factory);
			date.setObjectFieldsFrom(d);
			List<DateTime> result = date.getRunTimeElements(nextWeek).getStartTimes();
			assertEquals(3, result.size());
			Iterator<DateTime> it = result.iterator();
			while(it.hasNext()) {
				DateTime dt = it.next();
				String time = fmtTime.print(dt);
				assertEquals(DateTimeConstants.MONDAY, dt.getDayOfWeek() );
				assertEquals(true, time.equals("15:00:00") || time.equals("19:00:05") || time.equals("11:00:00"));
			}
		}
	}

	@Test
	public final void testGetDay() {
		xml = "<run_time>" +
		"<weekdays>" +
		"<day day=\"1\">" +
		"<period single_start=\"15:00\" />" +
		"</day>" +
		"<day day=\"2\">" +
		"<period single_start=\"17:00\" />" +
		"</day>" +
		"</weekdays>" +
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);

		List<Day> days = runtime.getWeekdays().getDay();
		assertEquals(2, days.size());
	}

	@Test
	public final void testMultipleSingleDays() {
		xml = "<run_time>" +
		"<weekdays>" +
		"<day day=\"1\">" +
		"<period single_start=\"15:00\" />" +
		"</day>" +
		"<day day=\"3\">" +
		"<period single_start=\"17:00\" />" +
		"</day>" +
		"</weekdays>" +
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);
		List<DateTime> result = runtime.getJsObjWeekdays().getRunTimeElements(nextWeek).getStartTimes();
		assertEquals(2, result.size());
		Iterator<DateTime> it = result.iterator();
		while(it.hasNext()) {
			DateTime dt = it.next();
			String time = fmtTime.print(dt);
			assertEquals(true, dt.getDayOfWeek() == DateTimeConstants.MONDAY || dt.getDayOfWeek() == DateTimeConstants.WEDNESDAY );
			assertEquals(true, time.equals("15:00:00") || time.equals("17:00:00"));
		}

	}

	@Test
	public final void testMultipleDays() {
		xml = "<run_time>" +
		"<weekdays>" +
		"<day day=\"1 4\">" +
		"<period single_start=\"15:00\" />" +
		"</day>" +
		"</weekdays>" +
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);
		List<DateTime> result = runtime.getDtSingleStarts(nextWeek);
		assertEquals(2, result.size());
		Iterator<DateTime> it = result.iterator();
		while(it.hasNext()) {
			DateTime dt = it.next();
			String time = fmtTime.print(dt);
			assertEquals(true, dt.getDayOfWeek() == DateTimeConstants.MONDAY || dt.getDayOfWeek() == DateTimeConstants.THURSDAY );
			assertEquals(true, time.equals("15:00:00"));
		}

	}

	@Test
	public final void testMissingPeriod() {
		xml = "<run_time>" +
		"<weekdays>" +
		"<day day=\"3\"/>" +
		"</weekdays>" +
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);

		Iterator<Day> itD = runtime.getWeekdays().getDay().iterator();
		while(itD.hasNext()) {
			Day d = itD.next();
			JSObjWeekdaysDay date = new JSObjWeekdaysDay(factory);
			date.setObjectFieldsFrom(d);
			List<DateTime> result = date.getRunTimeElements(nextWeek).getStartTimes();
			assertEquals(0, result.size());		// no entries, because period element is missing
		}
	}

	@Test
	public final void testAllMondaysInMonth() {
		xml = "<run_time>" +
		"<weekdays>" +
		"<day day=\"1\"/>" +
		"</weekdays>" +
		"</run_time>";
		DateTime from = new DateTime(2012,3,1,0,0,0,0);
		Interval month = new Interval(from,from.plusMonths(1));
		JSObjRunTime runtime = new JSObjRunTime(factoryDefaultPeriods,xml);
		for(DateTime d : runtime.getDtWeekdays(month).getStartTimes()) {
			logger.debug(fmtDateTime.print(d));
		}

//		Iterator<Day> itD = runtime.getWeekdays().getDay().iterator();
//		while(itD.hasNext()) {
//			Day d = itD.next();
//			JSObjWeekdaysDay date = new JSObjWeekdaysDay(factory);
//			date.setObjectFieldsFrom(d);
//			List<DateTime> result = date.getRunTimeElements(nextWeek).getStartTimes();
//			assertEquals(0, result.size());		// no entries, because period element is missing
//		}
	}


}
