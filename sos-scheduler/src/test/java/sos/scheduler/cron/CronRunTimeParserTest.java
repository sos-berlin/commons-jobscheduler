package sos.scheduler.cron;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CronRunTimeParserTest {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(this.getClass());
    private static final String conTagRUN_TIME = "run_time";

    private final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder docBuilder = null;
    private Document objOrderDocument = null;
    private Element runTimeElement = null;
    private CronRunTimeParser objP = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        docBuilder = docFactory.newDocumentBuilder();

        objOrderDocument = docBuilder.newDocument();
        BasicConfigurator.configure();
        logger.getRootLogger().setLevel(Level.DEBUG);
        runTimeElement = objOrderDocument.createElement(conTagRUN_TIME);
        objP = new CronRunTimeParser("00 20 * * 2,6 /rwe/oracle/scripts/backup/Rman_Backup.ksh STBCR1P LEVEL0 2>&1 > /dev/null");

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCronRunTimeParser() {
        objP.createRunTime(runTimeElement, "01 10 * * 3 /rwe/oracle/scripts/maintenance/log_clean_up/log_clean_up.ksh 2>&1 > /dev/null");
        showXML();
    }

    @Test
    public void testGetUseNewRunTime() {
        // fail("Not yet implemented");
    }

    @Test
    public void testGetRunTimeAsXML() throws Exception {

        objP.createRunTime(runTimeElement, "35 * * * * /rwe/oracle/scripts/maintenance/check_rman_backups.ksh 2>&1 > /dev/null");
        showXML();
    }

    @Test
    public void testCreateRunTime() throws Exception {
        objP.createRunTime(runTimeElement, "00 21 * * 0,1,3,4,5 /rwe/oracle/scripts/backup/Rman_Backup.ksh STBCL1P LEVEL1 2>&1 > /dev/null");
        showXML();
    }

    private void showXML() {
        logger.debug("\n" + objP.getRunTimeAsXML());

    }
}
