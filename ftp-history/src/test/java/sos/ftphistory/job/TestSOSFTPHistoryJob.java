package sos.ftphistory.job;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sos.connection.SOSConnection;

public class TestSOSFTPHistoryJob {

    public TestSOSFTPHistoryJob() {
        // TODO Auto-generated constructor stub
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGUID() throws Exception {

        SOSFTPHistoryJob ftpHistoryJob = new SOSFTPHistoryJob();
        ftpHistoryJob.init();
        ftpHistoryJob.setConnection(SOSConnection.createInstance("SOSOracleConnection", "oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:thin:@localhost:1521:test", "test", "test"));
        String randomUUIDString = UUID.randomUUID().toString();
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("guid", randomUUIDString);
        String guid = ftpHistoryJob.getRecordValue(parameters, "mapping_guid");
        assertTrue("guid is not reduced", guid.length() == randomUUIDString.length());
    }

}
