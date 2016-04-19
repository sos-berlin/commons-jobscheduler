package sos.scheduler.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.io.Files.JSFile;

/** @author KB */
public class JobSchedulerFileOperationsBaseTest {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerFileOperationsBaseTest.class);
    private static final String TEST_DATA_DIR = "R:/nobackup/junittests/testdata/JobSchedulerFileOperationsBase";
    private static JobSchedulerFileOperationBase objFOP = null;
    private static HashMap<String, String> params = new HashMap<String, String>();
    private JSFile objFile = null;
    private final String strTestFileName = System.getProperty(JobSchedulerFileOperationBase.conPropertyJAVA_IO_TMPDIR) + "/testcheckSteadyStateOfFiles.t";

    public JobSchedulerFileOperationsBaseTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objFOP = new JobSchedulerFileOperationBase();
        params.put("boolean", "true");
        objFOP.setParams(params);
    }

    @Test
    public final void testCompareIntValues() {
        assertTrue("must be true", objFOP.compareIntValues("eq", 1, 1));
        assertTrue("must be true", objFOP.compareIntValues("ne", 0, 1));
        assertTrue("must be true", objFOP.compareIntValues("le", 0, 1));
        assertTrue("must be true", objFOP.compareIntValues("lt", 0, 1));
        assertTrue("must be true", objFOP.compareIntValues("ge", 2, 1));
        assertTrue("must be true", objFOP.compareIntValues("gt", 2, 1));
        assertTrue("must be true", objFOP.compareIntValues("=", 1, 1));
        assertTrue("must be true", objFOP.compareIntValues("!=", 0, 1));
        assertTrue("must be true", objFOP.compareIntValues("<=", 0, 1));
        assertTrue("must be true", objFOP.compareIntValues("<", 0, 1));
        assertTrue("must be true", objFOP.compareIntValues(">=", 2, 1));
        assertTrue("must be true", objFOP.compareIntValues(">", 2, 1));
        assertFalse("must be false", objFOP.compareIntValues("eq", 1, 0));
        assertFalse("must be false", objFOP.compareIntValues("ne", 0, 0));
        assertFalse("must be false", objFOP.compareIntValues("le", 3, 1));
        assertFalse("must be false", objFOP.compareIntValues("lt", 3, 1));
        assertFalse("must be false", objFOP.compareIntValues("ge", 1, 2));
        assertFalse("must be false", objFOP.compareIntValues("gt", 2, 5));
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public final void testCompareIntValuesWithException() {
        assertTrue("must give an exception", objFOP.compareIntValues(">>", 1, 1));
    }

    @Test
    public final void testGetParamBoolean() {
        boolean flgB = false;
        assertTrue("must be true", objFOP.getParamBoolean("boolean", true));
        params.put("boolean", "false");
        assertFalse("must be false", objFOP.getParamBoolean("boolean", true));
    }

    @Test
    public final void testGetParamLong() {
        params.put("maxValue", "4711");
        assertEquals("must be 4711", (long) 4711, objFOP.getParamLong("maxValue", 0));
    }

    @Test
    public final void testGetParamIntegerStringInt() {
        params.put("maxValue", "4711");
        assertEquals("must be 4711", 4711, objFOP.getParamInteger("maxValue", 0));
    }

    @Test
    public final void testGetParamValue() {
        params.put("maxValue", "4711");
        assertEquals("must be 4711", "4711", objFOP.getParamValue("maxValue"));
    }

    @Test
    public final void testGetParamValueWithAlias() {
        params.put(JobSchedulerFileOperationBase.conParameterFILE, "4711");
        params.put(JobSchedulerFileOperationBase.conParameterFILE_PATH, "");
        params.put(JobSchedulerFileOperationBase.conParameterSOURCE_FILE, "");
        assertEquals(
                "must be 4711",
                "4711",
                objFOP.getParamValue(new String[] { JobSchedulerFileOperationBase.conParameterFILE, JobSchedulerFileOperationBase.conParameterFILE_PATH,
                        JobSchedulerFileOperationBase.conParameterSOURCE_FILE }, ""));
        params.put(JobSchedulerFileOperationBase.conParameterFILE, "");
        params.put(JobSchedulerFileOperationBase.conParameterFILE_PATH, "4711");
        params.put(JobSchedulerFileOperationBase.conParameterSOURCE_FILE, "");
        assertEquals(
                "must be 4711",
                "4711",
                objFOP.getParamValue(new String[] { JobSchedulerFileOperationBase.conParameterFILE, JobSchedulerFileOperationBase.conParameterFILE_PATH,
                        JobSchedulerFileOperationBase.conParameterSOURCE_FILE }, ""));
        params.put(JobSchedulerFileOperationBase.conParameterFILE, "");
        params.put(JobSchedulerFileOperationBase.conParameterFILE_PATH, "");
        params.put(JobSchedulerFileOperationBase.conParameterSOURCE_FILE, "4711");
        assertEquals(
                "must be 4711",
                "4711",
                objFOP.getParamValue(new String[] { JobSchedulerFileOperationBase.conParameterFILE, JobSchedulerFileOperationBase.conParameterFILE_PATH,
                        JobSchedulerFileOperationBase.conParameterSOURCE_FILE }, ""));
    }

    @Test
    public final void testGetParamValueWithAliasWithoutUnderscore() {
        params.put(JobSchedulerFileOperationBase.conParameterFILE.replaceAll("_", ""), "");
        params.put(JobSchedulerFileOperationBase.conParameterFILE_PATH.replaceAll("_", ""), "4711");
        params.put(JobSchedulerFileOperationBase.conParameterSOURCE_FILE.replaceAll("_", ""), "");
        assertEquals(
                "must be 4711",
                "4711",
                objFOP.getParamValue(new String[] { JobSchedulerFileOperationBase.conParameterFILE, JobSchedulerFileOperationBase.conParameterFILE_PATH,
                        JobSchedulerFileOperationBase.conParameterSOURCE_FILE }, ""));
    }

    @Test
    public final void testIsGraciousAll() throws Exception {
        params.put(JobSchedulerFileOperationBase.conParameterGRACIOUS, "all");
        objFOP.getParametersFromHashMap();
        assertTrue("must be true", objFOP.isGraciousAll());
    }

    class WriteToFile implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 15; i++) {
                LOGGER.debug(i);
                try {
                    objFile.Write(i + ": This is a test");
                    objFile.WriteLine(i + ": This is a test");
                    objFile.WriteLine(i + ": This is a test");
                    Thread.sleep(500);
                    objFile.WriteLine(i + ": This is a test");
                    Thread.sleep(500);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            LOGGER.debug("finished");
            try {
                objFile.close();
                objFile = null;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void testcheckSteadyStateOfFiles() {
        if (objFOP.lstResultList == null) {
            objFOP.saveResultList();
        }
        params.put(JobSchedulerFileOperationBase.conParameterCHECK_STEADYSTATEOFFILE, "true");
        objFOP.setParams(params);
        objFile = new JSFile(strTestFileName);
        objFOP.lstResultList.add(objFile);
        objFOP.flgOperationWasSuccessful = true;
        objFOP.flgCheckSteadyStateOfFiles = true;
        Thread thread = new Thread(new WriteToFile());
        thread.start();
        assertTrue("must be true", objFOP.checkSteadyStateOfFiles());
    }

    @Test
    public final void testcheckSteadyStateOfFiless() {
        if (objFOP.lstResultList == null) {
            objFOP.saveResultList();
        }
        params.put(JobSchedulerFileOperationBase.conParameterCHECK_STEADYSTATEOFFILE, "true");
        params.put(JobSchedulerFileOperationBase.conParameterCHECK_STEADYSTATEINTERVAL, "1500");
        objFOP.setParams(params);
        objFile = new JSFile(TEST_DATA_DIR + "/test.ping");
        objFOP.lstResultList.add(objFile);
        objFOP.flgOperationWasSuccessful = true;
        objFOP.flgCheckSteadyStateOfFiles = true;
        assertTrue("must be true", objFOP.checkSteadyStateOfFiles());
    }

    @Test
    public final void testIsGraciousTrue() throws Exception {
        params.put(JobSchedulerFileOperationBase.conParameterGRACIOUS, "true");
        objFOP.getParametersFromHashMap();
        assertTrue("must be true", objFOP.isGraciousTrue());
    }

    @Test
    public final void testIsGraciousFalse() throws Exception {
        params.put(JobSchedulerFileOperationBase.conParameterGRACIOUS, "false");
        objFOP.getParametersFromHashMap();
        assertFalse("must be false", objFOP.isGraciousTrue());
    }

    @Test
    public final void testReplaceVars() throws Exception {
        params.put("replaceVars", "%" + JobSchedulerFileOperationBase.conParameterGRACIOUS + "%");
        params.put(JobSchedulerFileOperationBase.conParameterGRACIOUS, "true");
        objFOP.getParametersFromHashMap();
        assertEquals("must be true", "true", objFOP.replaceVars4(objFOP.getParamValue("replaceVars")));
        params.put("replaceVars", "%notFound%");
        assertEquals("must be true", "?notFound?", objFOP.replaceVars4(objFOP.getParamValue("replaceVars")));
    }

    @Test
    public final void testSetReturnResult() throws Exception {
        params.put(JobSchedulerFileOperationBase.conParameterGRACIOUS, "false");
        objFOP.getParametersFromHashMap();
        boolean flgRet = objFOP.setReturnResult(true);
        assertEquals("Job has successful ended", JobSchedulerJobAdapter.conJobSuccess, flgRet);
        flgRet = objFOP.setReturnResult(false);
        assertEquals("Job has ended with failure", JobSchedulerJobAdapter.conJobFailure, flgRet);
        params.put(JobSchedulerFileOperationBase.conParameterGRACIOUS, "true");
        objFOP.getParametersFromHashMap();
        assertEquals("Job has ended with failure", JobSchedulerJobAdapter.conJobSuccess, objFOP.setReturnResult(false));
        params.put(JobSchedulerFileOperationBase.conParameterGRACIOUS, "all");
        objFOP.getParametersFromHashMap();
        assertEquals("Job has ended with failure", JobSchedulerJobAdapter.conJobSuccess, objFOP.setReturnResult(false));
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public final void testCheckMandatoryFile() {
        objFOP.CheckMandatoryFile();
        objFOP.CheckMandatorySource();
    }

    @Test
    public final void testCheckMandatoryFile2() throws Exception {
        params.put(JobSchedulerFileOperationBase.conParameterFILE, "huhuhu");
        objFOP.getParametersFromHashMap();
        objFOP.CheckMandatoryFile();
    }

    @Test
    public final void testCheckMandatorySource() throws Exception {
        params.put(JobSchedulerFileOperationBase.conParameterSOURCE_FILE, "huhuhu");
        objFOP.getParametersFromHashMap();
        objFOP.CheckMandatorySource();
    }

    @Test
    public final void testCalculateFileAge() {
        long lngAge = objFOP.calculateFileAge("00:01:00");
        assertEquals("must equal to 1 minute", (long) 60 * 1000, lngAge);
        lngAge = objFOP.calculateFileAge("01:01:00");
        assertEquals("must equal to 1 minute", (long) 3660000, lngAge);
        lngAge = objFOP.calculateFileAge("0101");
        assertEquals("must equal to 1 minute", (long) 101, lngAge);
        lngAge = objFOP.calculateFileAge(null);
        assertEquals("must equal to 1 minute", (long) 0, lngAge);
    }

    @Test(expected = java.lang.NumberFormatException.class)
    public final void testCalculateFileAge2() {
        long lngAge = objFOP.calculateFileAge("abcdef");
        assertEquals("must equal to 1 minute", (long) 0, lngAge);
    }

}
