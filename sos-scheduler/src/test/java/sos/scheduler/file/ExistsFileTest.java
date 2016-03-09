package sos.scheduler.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.io.Files.JSFile;

/** @author oh */
public class ExistsFileTest extends JSToolBox {

    private static final Logger LOGGER = Logger.getLogger(ExistsFileTest.class);
    private JSFile objFile = null;
    private final String strTestFileName = System.getProperty(JobSchedulerFileOperationBase.conPropertyJAVA_IO_TMPDIR) + "/testcheckSteadyStateOfFiles.t";

    public ExistsFileTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        String strLog4JFileName = "./log4j.properties";
        String strT = new File(strLog4JFileName).getAbsolutePath();
        LOGGER.info("log4j properties filename = " + strT);
    }

    public void createFiles(final HashMap<String, String> params) throws Exception {
        int fileSize;
        long fileAge;
        if (params.containsKey("create_file") && isNotEmpty(params.get("create_file"))) {
            if (params.containsKey("file_size") && isNotEmpty(params.get("file_size"))) {
                fileSize = Integer.parseInt(params.get("file_size"));
            } else {
                fileSize = 10;
            }
            if (params.containsKey("file_age") && isNotEmpty(params.get("file_age"))) {
                fileAge = System.currentTimeMillis() - 1000 * Long.parseLong(params.get("file_age"));
            } else {
                fileAge = System.currentTimeMillis();
            }
            populateFile(new File(params.get("create_file")), fileSize, fileAge);
        }
        for (int i = 0; i < 20; i++) {
            if (params.containsKey("create_file_" + i) && isNotEmpty(params.get("create_file_" + i))) {
                if (params.containsKey("file_size_" + i) && isNotEmpty(params.get("file_size_" + i))) {
                    fileSize = Integer.parseInt(params.get("file_size_" + i));
                } else {
                    fileSize = 10;
                }
                if (params.containsKey("file_age_" + i) && isNotEmpty(params.get("file_age_" + i))) {
                    fileAge = System.currentTimeMillis() - 1000 * Long.parseLong(params.get("file_age_" + i));
                } else {
                    fileAge = System.currentTimeMillis();
                }
                populateFile(new File(params.get("create_file_" + i)), fileSize, fileAge);
            }
        }
    }

    public void populateFile(final File file, final int fileSize, final long lastModified) throws Exception {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(new byte[fileSize]);
            LOGGER.info(file.getAbsolutePath() + " created: SIZE[" + fileSize + "] MODIFIED[" + lastModified + "]");
        } catch (Exception e) {
            throw new Exception("could not populate file [" + file.getAbsolutePath() + "]: " + e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                    file.setLastModified(lastModified);
                } catch (Exception x) {
                }
            }
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testExistsFile() throws Exception {
        String location = "Z:/scheduler.test/testsuite_files/files/file_operations/exists/13/in/";
        HashMap<String, String> objT = new HashMap<String, String>();
        objT.put("file", location);
        objT.put("file_spec", ".*");
        objT.put("min_file_age", "60");
        objT.put("max_file_age", "24:30:00");
        objT.put("expected_size_of_result_set", "3");
        objT.put("raise_error_if_result_set_is", "!=");
        objT.put("create_file_1", location + "1.dat");
        objT.put("create_file_2", location + "2.dat");
        objT.put("create_file_3", location + "3.dat");
        objT.put("create_file_4", location + "4.dat");
        objT.put("create_file_5", location + "5.dat");
        objT.put("create_file_6", location + "6.dat");
        objT.put("create_file_7", location + "7.dat");
        objT.put("create_file_8", location + "8.dat");
        objT.put("file_age_1", "0");
        objT.put("file_age_2", "10");
        objT.put("file_age_3", "30");
        objT.put("file_age_4", "120");
        objT.put("file_age_5", "3600");
        objT.put("file_age_6", "86400");
        objT.put("file_age_7", "90000");
        objT.put("file_age_8", "172800");
        createFiles(objT);
        JSExistsFile objR = new JSExistsFile();
        JSExistsFileOptions objO = objR.Options();
        objO.setAllOptions(objT);
        LOGGER.info(objO.on_empty_result_set.isDirty());
        objR.Execute();
        Vector<File> lstResultList = objR.getResultList();
        LOGGER.info(lstResultList.size());
        LOGGER.info(lstResultList);
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

}
