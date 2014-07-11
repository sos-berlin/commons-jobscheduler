package com.sos.JSHelper.Options;

import org.junit.Test;

public class SOSOptionStringWVariablesTest {
	@Test public void testValue() {
//		fail("Not yet implemented");
	}

	@Test public void testValueString() {
//		fail("Not yet implemented");
	}

	@Test public void testSOSOptionStringWVariables() {
//		fail("Not yet implemented");
	}

	@Test public void testSubstituteAllDate() {
//		fail("Not yet implemented");
	}

	@Test public void testOriginalValue() {
		SOSOptionStringWVariables objS = new SOSOptionStringWVariables("date [date:yyyyMMdd] hat was");
		System.out.println("objS.Value() " +objS.Value());
		System.out.println("objS.OriginalValue() " + objS.OriginalValue());
		System.out.println("objS.Value() " + objS.Value());
	}

	@Test public void testDoReSubstitution() {
//		fail("Not yet implemented");
	}

	@Test public void testDoReplace() {
		SOSOptionStringWVariables objS = new SOSOptionStringWVariables("date [uuid:] hat was, [timestamp:], [sqltimestamp:]");
		System.out.println(objS.Value());
		System.out.println(objS.Value());
		System.out.println(objS.Value());
		System.out.println(objS.Value());
	}

	@Test  (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)  public void testDoReplace2() {
		SOSOptionStringWVariables objS = new SOSOptionStringWVariables("date [uuid:] hat was, [timestamp:], [sqltimestamp]");
		String strT = objS.Value();
		System.out.println(strT);
	}

	@Test public void testSubstituteAllDateString() {
//		fail("Not yet implemented");
	}

	@Test public void testGetVariablePart() {
//		fail("Not yet implemented");
	}

	@Test public void testSubstituteAllFilename() {
//		fail("Not yet implemented");
	}

	@Test public void testGetUUID() {
		SOSOptionStringWVariables objS = new SOSOptionStringWVariables("date [uuid:] hat was");
		System.out.println(objS.Value());
		System.out.println(objS.Value());
		System.out.println(objS.Value());
		System.out.println(objS.Value());
	}

	@Test public void testGetUnixTimeStamp() {
		SOSOptionStringWVariables objS = new SOSOptionStringWVariables("date [timestamp:] hat was");
		System.out.println(objS.Value());
		System.out.println(objS.Value());
		System.out.println(objS.Value());
		System.out.println(objS.Value());
	}

	@Test public void testGetSqlTimeStamp() {
		SOSOptionStringWVariables objS = new SOSOptionStringWVariables("date [sqltimestamp:] hat was");
		System.out.println(objS.Value());
		System.out.println(objS.Value());
		System.out.println(objS.Value());
		System.out.println(objS.Value());
	}
}
