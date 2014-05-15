package com.sos.VirtualFileSystem.shell;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Logging.Log4JHelper;

public class cmdShellTest {

	@SuppressWarnings("unused")
	private static final String	conSVNVersion			= "$Id$";
	private static final Logger	logger					= Logger.getLogger(cmdShellTest.class);
	private static Log4JHelper	objLogger				= null;

	private final 		cmdShell objShell = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		Log4JHelper.flgUseJobSchedulerLog4JAppender = false;
		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$
		objLogger.setLevel(Level.DEBUG);

		String osn = System.getProperty("os.name");
		String fcp = System.getProperty("file.encoding");
		String ccp = System.getProperty("console.encoding");

		logger.info(osn + ", fcp =  " + fcp + ", ccp = " + ccp);
		cmdShell objShell = new cmdShell();

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCmdShell() {
//		fail("Not yet implemented");
	}

	@Test
	public void testGetStdOut() {
//		fail("Not yet implemented");
	}

	@Test
	public void testGetStdErr() {
//		fail("Not yet implemented");
	}

//	@Test
	public void testExecuteCommand() throws Exception {

		cmdShell objShell = new cmdShell();
		int intCC = 0;
//		 intCC = objShell.executeCommand("C:/Users/KB/Desktop/filezilla_start.bat");
		intCC = objShell.executeCommand("dir");
//		intCC = objShell.executeCommand("dir bin");
		// xcopy bleibt hängen. scheint von stdin lesen zu wollen und bekommt nichts?
		// Mit echo 1 | sqlcmd funktioniert es. Ohne geht es nicht bei älteren Versionen.
//		intCC = objShell.executeCommand("xcopy conf c:\\temp\\conf /F /Y");
//		intCC = objShell.executeCommand("xcopy bin c:\\temp\\bin /I /F /Y /J");
//		logger.debug(SOSVfs_D_231.params(intCC));

//		fail("Not yet implemented");
	}
 // C:/oraclexe/app/oracle/product/11.2.0/server/bin/sqlplus.exe /nolog


	@Test
	public void testExecuteSQLPlus() throws Exception {

		cmdShell objShell = new cmdShell();
		int intCC = 0;
//		 intCC = objShell.executeCommand("C:/Users/KB/Desktop/filezilla_start.bat");
		intCC = objShell.executeCommand("echo 1 | \"C:/oraclexe/app/oracle/product/11.2.0/server/bin/sqlplus.exe\" -S -L sys/scheduler@localhost as sysdba @c:/temp/mycmd.sql");
//		intCC = objShell.executeCommand("dir bin");
		// xcopy bleibt hängen. scheint von stdin lesen zu wollen und bekommt nichts?
		// Mit echo 1 | sqlcmd funktioniert es. Ohne geht es nicht bei älteren Versionen.
//		intCC = objShell.executeCommand("xcopy conf c:\\temp\\conf /F /Y");
//		intCC = objShell.executeCommand("xcopy bin c:\\temp\\bin /I /F /Y /J");
//		logger.debug(SOSVfs_D_231.params(intCC));
		System.out.println("intCC = " + intCC);
//		fail("Not yet implemented");
	}

}
