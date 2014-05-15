package com.sos.scheduler.model.objects;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.LiveConnector;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.TestBase;
import com.sos.scheduler.model.tools.PathResolver;
import com.sos.scheduler.model.tools.RunTimeElement;
import com.sos.scheduler.model.tools.RunTimeElements;

public class JSObjHolidaysTest extends TestBase {

	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(JSObjHolidaysTest.class);
	
	private static SchedulerObjectFactory factory = null;
	private final static DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");

	private String xml;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		factory = new SchedulerObjectFactory("8of9.sos", 4210);
		factory.initMarshaller(Spooler.class);
	}

	@Test
	public final void testWeekdays() {
		xml = "<run_time>" +
		"<holidays>" + 
		"<weekdays>" + 
		"<day day=\"6\"/>" + 
		"<day day=\"7\"/>" + 
		"</weekdays>" +
		"</holidays>" +
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);

		DateTime from = new DateTime(2012,3,1,0,0,0,0);
		DateTime to = from.plusMonths(1);
		List<DateTime> result = runtime.getJsObjHolidays().getHolidays( new Interval(from,to));
		assertEquals(9, result.size());
		assertEquals(DateTimeConstants.SATURDAY, result.get(0).getDayOfWeek() );
		assertEquals(DateTimeConstants.SUNDAY, result.get(1).getDayOfWeek() );
	}

	@Test
	public final void testHolidays() {
		xml = "<run_time>" +
		"<holidays>" + 
		"<holiday date=\"2012-03-31\"/>" + 
		"<holiday date=\"2012-04-01\"/>" + 
		"</holidays>" + 
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);
		DateTime from = new DateTime(2012,3,26,0,0,0,0);
		List<DateTime> result = runtime.getJsObjHolidays().getHolidays( new Interval(from,from.plusWeeks(1)));
		assertEquals(2, result.size());
		assertEquals(DateTimeConstants.SATURDAY, result.get(0).getDayOfWeek() );
		assertEquals(DateTimeConstants.SUNDAY, result.get(1).getDayOfWeek() );
	}

	@Test
	public final void testIsHolidayViaWeekdays() {
		xml = "<run_time>" +
		"<holidays>" + 
		"<weekdays>" + 
		"<day day=\"6\"/>" + 
		"<day day=\"7\"/>" + 
		"</weekdays>" +
		"</holidays>" +
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);

		DateTime testDate = new DateTime(2012,3,31,0,0,0,0);
		JSObjHolidays holidays = runtime.getJsObjHolidays();
		assertEquals(false, holidays.isHoliday(testDate.minusDays(1)));
		assertEquals(false, holidays.isHoliday(testDate.plusDays(2)));
		assertEquals(true, holidays.isHoliday(testDate));
	}

	@Test
	public final void testIsHolidayViaHoliday() {
		xml = "<run_time>" +
		"<holidays>" + 
		"<holiday date=\"2012-03-31\"/>" + 
		"<holiday date=\"2012-04-01\"/>" + 
		"</holidays>" + 
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);
		DateTime testDate = new DateTime(2012,3,31,0,0,0,0);
		JSObjHolidays holidays = runtime.getJsObjHolidays();
		assertEquals(false, holidays.isHoliday(testDate.minusDays(1)));
		assertEquals(false, holidays.isHoliday(testDate.plusDays(2)));
		assertEquals(true, holidays.isHoliday(testDate));
	}

	@Test
	public final void testGetNextNonHoliday() {
		xml = "<run_time>" +
		"<holidays>" + 
		"<holiday date=\"2012-03-31\"/>" + 
		"<holiday date=\"2012-04-01\"/>" + 
		"</holidays>" + 
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);
		DateTime testDate = new DateTime(2012,3,31,0,0,0,0);
		DateTime result = runtime.getJsObjHolidays().getNextNonHoliday(testDate);
		assertEquals("2012-04-02", fmtDate.print(result));
	}

	@Test
	public final void testGetPreviousNonHoliday() {
		xml = "<run_time>" +
		"<holidays>" + 
		"<holiday date=\"2012-03-31\"/>" + 
		"<holiday date=\"2012-04-01\"/>" + 
		"</holidays>" + 
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);
		DateTime testDate = new DateTime(2012,3,31,0,0,0,0);
		DateTime result = runtime.getJsObjHolidays().getPreviousNonHoliday(testDate);
		assertEquals("2012-03-30", fmtDate.print(result));
	}

	@Test
	public final void testStartDatesAwareHolidays() {
		xml = "<run_time>" +
		"<holidays>" + 
		"<holiday date=\"2012-03-31\"/>" + 
		"</holidays>" + 
		"</run_time>";
		JSObjRunTime runtime = new JSObjRunTime(factory,xml);
		RunTimeElements runTimes = new RunTimeElements( new DateTime(2012,3,31,0,0,0,0) );

		runTimes.add( new RunTimeElement( new DateTime(2012,3,31,0,0,0,0),WhenHoliday.IGNORE_HOLIDAY) );
		List<DateTime> result = runtime.getJsObjHolidays().getStartDatesAwareHolidays(runTimes); 
		assertEquals(1,result.size());

		runTimes.clear();
		runTimes.add( new RunTimeElement( new DateTime(2012,3,31,0,0,0,0),WhenHoliday.PREVIOUS_NON_HOLIDAY) );
		result = runtime.getJsObjHolidays().getStartDatesAwareHolidays(runTimes); 
		assertEquals(0,result.size());

		runTimes.clear();
		runTimes.add( new RunTimeElement( new DateTime(2012,3,31,0,0,0,0),WhenHoliday.NEXT_NON_HOLIDAY) );
		result = runtime.getJsObjHolidays().getStartDatesAwareHolidays(runTimes); 
		assertEquals(0,result.size());

		runTimes.clear();
		runTimes.add( new RunTimeElement( new DateTime(2012,3,30,0,0,0,0),WhenHoliday.IGNORE_HOLIDAY) );
		result = runtime.getJsObjHolidays().getStartDatesAwareHolidays(runTimes); 
		assertEquals(0,result.size());

		
		Interval i = new Interval(new DateTime(2012,3,30,0,0,0,0),new DateTime(2012,4,1,23,59,59,999));
		runTimes = new RunTimeElements(i);
		
		runTimes.clear();
		runTimes.add( new RunTimeElement( new DateTime(2012,3,31,0,0,0,0),WhenHoliday.SUPPRESS) );
		result = runtime.getJsObjHolidays().getStartDatesAwareHolidays(runTimes); 
		assertEquals(0,result.size());

		runTimes.clear();
		runTimes.add( new RunTimeElement( new DateTime(2012,3,31,0,0,0,0),WhenHoliday.PREVIOUS_NON_HOLIDAY) );
		result = runtime.getJsObjHolidays().getStartDatesAwareHolidays(runTimes); 
		assertEquals(1,result.size());
		assertEquals("2012-03-30",fmtDate.print(result.get(0)));

		runTimes.clear();
		runTimes.add( new RunTimeElement( new DateTime(2012,3,31,0,0,0,0),WhenHoliday.NEXT_NON_HOLIDAY) );
		result = runtime.getJsObjHolidays().getStartDatesAwareHolidays(runTimes); 
		assertEquals(1,result.size());
		assertEquals("2012-04-01",fmtDate.print(result.get(0)));

	}

	/**
	 * \brief Test the definition of holidays via an include file (live_file Attribute)
	 * \detail
	 * The order holidays-test contains an include for defining holidays at the weekend (Saturday and Sunday).
	 * To call the method getJsObjHolidays() of the runtime object will resolve the include implicitely and
	 * therefore we can get the holidays as usaul via the holidays object.
	 * To run this test it is necessary that the folder testdata is declared as source folder
	 * @throws IOException 
	 */
	@Test
	public final void testIncludeLive() throws IOException {
		
		LiveConnector connector = new LiveConnector( getLiveFolder() );
		SchedulerObjectFactory includeFactory = new SchedulerObjectFactory(connector);
		
		// the order holidays_test contains a relative reference to the inlcude holidays.xml in the root of the live folder
		testInclude(includeFactory, getResource("include/holidays-test.order.xml"));
		
		// the order weekdays_test contains a relative reference to the inlcude weekdays.xml in the same directory
		testInclude(includeFactory, getResource("include/weekdays-test.order.xml"));
		
		// the absolute-file-test contains a absolute reference to the inlcude weekdays.xml using an environment variable
		File from = new File(getResource("weekdays.xml"));
		File to = new File(System.getenv("TMP") + "/weekdays.xml");
		Files.copy(from, to);
		testInclude(includeFactory, getResource("include/absolute-file-test.order.xml") );
	}
	
	@Test(expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	/*
	 * To run this test it is necessary that the folder testdata is declared as source folder
	 */
	public final void testIncludeLiveInvalid() throws MalformedURLException {
		
		LiveConnector connector = new LiveConnector( getLiveFolder() );
		SchedulerObjectFactory includeFactory = new SchedulerObjectFactory(connector);
		
		// the relative-file-test contains a relative reference to the inlcude weekdays.xml using an environment variable
		String orderFile = getResource("include/relative-file-test.order.xml");
		testInclude(includeFactory, orderFile);
		
	}
	
	private final void testInclude(final SchedulerObjectFactory factory, final String configurationFile) {
		ISOSVirtualFile vfOrder = factory.getFileHandleOrNull(configurationFile);
		JSObjOrder order = new JSObjOrder(factory, vfOrder);
		JSObjRunTime runtime = order.getJSObjRunTime();
		assertEquals(configurationFile, PathResolver.normalizePath(order.getHotFolderSrc().getName()) );
		assertEquals(configurationFile, PathResolver.normalizePath(runtime.getHotFolderSrc().getName()) );  // runtime is based on the order file, too

		DateTime start = new DateTime(2012,4,23,0,0,0,0);
		Interval interval = new Interval(start,start.plusDays(7));
		List<DateTime> result = runtime.getJsObjHolidays().getHolidays(interval);
		assertEquals("2012-04-28", fmtDate.print(result.get(0)) );
		assertEquals("2012-04-29", fmtDate.print(result.get(1)) );
	}
	
}
