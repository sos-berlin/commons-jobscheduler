package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Test;

public class SOSOptionTimeHorizonTest {

	@SuppressWarnings("unused")
	private final String		conClassName	= "SOSOptionTimeHorizonTest";
	private static final Logger	logger			= Logger.getLogger(SOSOptionTimeHorizonTest.class);

	public SOSOptionTimeHorizon	timeHorizon		= new SOSOptionTimeHorizon( // ...
														null, // ....
														conClassName + ".variablename", // ...
														"OptionDescription", // ...
														"1:00:00:00", // ...
														"1:00:00:00", // ...
														true);

	public SOSOptionTimeHorizonTest() {
		//
	}

	@Test
	public final void testValueString() {
//		String expected = "+1:59:00:00";
//		String expected = "-60:00:00:00";
		String expected = "-60";
		timeHorizon.Value(expected);
		assertEquals(expected, timeHorizon.Value());
		System.out.println(timeHorizon.Value());
		System.out.println(timeHorizon.getEndFromNow().toString());
	}

	//	@Test(expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	//	public final void testValueString2(){
	//		timeHorizon.Value("471111");
	//		assertEquals("port is 471111", 4711, timeHorizon.value());
	//	}

	@Test public void testIsDirty () {
		timeHorizon.Value("-30");
		System.out.println(timeHorizon.isDirty());
	}
}
