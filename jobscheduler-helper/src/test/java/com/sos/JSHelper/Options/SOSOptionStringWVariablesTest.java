package com.sos.JSHelper.Options;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.io.Files.JSFile;

public class SOSOptionStringWVariablesTest {
	@SuppressWarnings("unused") private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused") private final Logger logger = Logger.getLogger(this.getClass());
	

	private SOSOptionStringWVariables objOption = null;

	@Before
	public void setUp() throws Exception {
		String strLog4JFileName = "./log4j.properties";
		String strT = new File(strLog4JFileName).getAbsolutePath();
		logger.info("logfilename = " + strT);

		objOption = new SOSOptionStringWVariables(null, "key", "Description","value", "DefaultValue", true);

	}


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

	@Test /*  (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class) */ public void testDoReplace2() {
		SOSOptionStringWVariables objS = new SOSOptionStringWVariables("date [uuid:] hat was, [timestamp:], [sqltimestamp]");
		String strT = objS.Value();
		System.out.println(strT);
	}

	@Test public void testSubstituteAllDateString() {
//		fail("Not yet implemented");
	}

	@Test public void testSubstituteTempFile() {
		String strT = objOption.substituteTempFile("this is a [tempfile:] name");
		logger.info(strT);
		assertTrue ("must not have a bracket:", strT.contains("[") == false);
	}

	@Test public void testEnvironmentVariable() {
		String strT = objOption.substituteEnvironmenVariable("this is a [env:username] name");
		logger.info(strT);
		assertTrue ("must not have a bracket:", strT.contains("[") == false);
	}

	@Test public void testSubstitureUUID() {
		String strT = objOption.substituteUUID("this is a [uuid:] uuid");
		logger.info(strT);
		assertTrue ("must not have a bracket:", strT.contains("[") == false);
	}

	@Test public void testSubstitureFileContent() throws Exception {
		JSFile objF = JSFile.createTempFile();
		objF.Write("Hallo, Welt...");
		objF.close();
		String strT = objOption.substituteFileContent("this is a '[file:"+ objF.getAbsolutePath() + "]' FileContent");
		logger.info(strT);
		assertTrue ("must not have a bracket:", strT.contains("[") == false);
	}

	@Test public void testSubstituteTimeStamp() {
		String strT = objOption.substituteTimeStamp("this is a [timestamp:] uuid");
		logger.info(strT);
		assertTrue ("must not have a bracket:", strT.contains("[") == false);
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
