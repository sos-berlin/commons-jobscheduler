package com.sos.scheduler.model.objects;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSObjPeriodTest {

	private final static Logger logger = Logger.getLogger(JSObjPeriodTest.class);
	private final static DateTimeFormatter fmtDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	private final static DateTimeFormatter fmtTime = DateTimeFormat.forPattern("HH:mm:ss");

	private static SchedulerObjectFactory factory = null;
	
	private String xml;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		factory = new SchedulerObjectFactory("8of9.sos", 4210);
		factory.initMarshaller(Spooler.class);
	}

	@Test
	public final void testSetValidXmlContent() {
		String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
		xml = "<period single_start=\"15:00\" xmlns:ns2=\"job-chain-extensions\"/>\n";
		JSObjPeriod period = new JSObjPeriod(factory,xml);
		assertEquals(xmlHeader + xml, period.toXMLString());
	}

	@Test(expected= JobSchedulerException.class)
	public final void testSetInvalidXmlContent() {
		xml = "<invalid period='' />";
		JSObjPeriod period = new JSObjPeriod(factory,xml);
		logger.debug(period.toXMLString());
	}

	@Test
	public final void testConstructorWithXmlContent() {
		String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
		xml = "<period single_start=\"15:00\" xmlns:ns2=\"job-chain-extensions\"/>\n";
		JSObjPeriod p = new JSObjPeriod(factory,xml);
		assertEquals(xmlHeader + xml, p.toXMLString());
	}

	@Test
	public final void testFlags() {
		xml = "<period single_start='15:00' />";
		JSObjPeriod period = new JSObjPeriod(factory,xml);
		assertEquals(true, period.hasSingleStart());
		assertEquals(false, period.hasStartStartInterval());
		assertEquals(false, period.hasEndStartInterval());

		xml = "<period begin='15:00' end='17:00' repeat='10:00' />";
		period = new JSObjPeriod(factory,xml);
		assertEquals(false, period.hasSingleStart());
		assertEquals(false, period.hasStartStartInterval());
		assertEquals(true, period.hasEndStartInterval());

		xml = "<period begin='15:00' end='17:00' absolute_repeat='10:00' />";
		period = new JSObjPeriod(factory,xml);
		assertEquals(false, period.hasSingleStart());
		assertEquals(true, period.hasStartStartInterval());
		assertEquals(false, period.hasEndStartInterval());
	}

	@Test
	public final void testSingleStart() {
		xml = "<period single_start='15:00' when_holiday='previous_non_holiday'/>";
		JSObjPeriod period = new JSObjPeriod(factory,xml);
		DateTime d = new DateTime(2012, 3, 7, 16, 0, 0, 0);
		assertEquals("2012-03-07 15:00:00", fmtDateTime.print(period.getDtSingleStartOrNull(d)) );
	}

	@Test
	public final void testNextSingleStart() {
		
		// single start in future
		DateTime single = new DateTime().plusMinutes(1);
		xml = "<period single_start='" + fmtTime.print(single) + "' />";
		JSObjPeriod period = new JSObjPeriod(factory,xml);
		assertEquals(fmtDateTime.print(single), fmtDateTime.print(period.getDtNextSingleStartOrNull()) );

		// single start in past
		single = new DateTime().minusMinutes(1);
		xml = "<period single_start='" + fmtTime.print(single) + "' />";
		period = new JSObjPeriod(factory,xml);
		assertEquals(fmtDateTime.print(single.plusDays(1)), fmtDateTime.print(period.getDtNextSingleStartOrNull()) );
	}

	@Test
	public final void testBeginAndEnd() {
		xml = "<period begin='15:00' end='17:30'/>";
		JSObjPeriod period = new JSObjPeriod(factory,xml);
		DateTime d = new DateTime(2012, 3, 8, 16, 0, 0, 0);
		assertEquals("2012-03-08 15:00:00", fmtDateTime.print(period.getDtBeginOrNull(d)) );
		assertEquals("2012-03-08 17:30:00", fmtDateTime.print(period.getDtEndOrNull(d)) );
	}

	@Test
	public final void testPeriod() {
		xml = "<period begin='15:00' end='17:00' />";
		DateTime dIn = new DateTime(2012, 3, 7, 16, 0, 0, 0);
		DateTime dOut = new DateTime(2012, 3, 7, 14, 59, 59, 0);
		JSObjPeriod period = new JSObjPeriod(factory,xml);
		assertEquals(true, period.isInPeriod(dIn));
		assertEquals(false, period.isInPeriod(dOut));
	}

	@Test
	public final void testGetBeginAndEnd() {
		
		xml = "<period begin='15:00' end='17:00' />";
		JSObjPeriod period = new JSObjPeriod(factory,xml);
		assertEquals("15:00:00", period.getBegin());
		assertEquals("17:00:00", period.getEnd());
		
		xml = "<period begin='15:00:50' end='17:00:12' />";
		period = new JSObjPeriod(factory,xml);
		assertEquals("15:00:50", period.getBegin());
		assertEquals("17:00:12", period.getEnd());
	}

	@Test
	public final void testGetSingleStartsOrNull() {
		xml = "<period single_start='17:00'/>";
		JSObjPeriod period = new JSObjPeriod(factory,xml);
		List<DateTime> starts = period.getRunTimeElements(IntervalConstants.NEXT_24H.getInterval()).getStartTimes();
		for(DateTime start : starts) {
			logger.debug("Start time: " + fmtDateTime.print(start));
		}
	}

}
