package sos.scheduler.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import sos.scheduler.job.JobSchedulerJob;
import sos.spooler.Variable_set;
import sos.util.SOSCrypt;

import com.sos.JSHelper.Basics.VersionInfo;

public class JobSchedulerMD5File extends JobSchedulerJob {

    private static final String FILENAME_EXTENSION_MD5 = ".md5";
    private static final String JOB_PARAMETER_FILE = "file";
    private static final String JOB_PARAM_MD5_SUFFIX = "md5_suffix";
    private static final String MODE_CREATE = "create";
    private static final String JOB_PARAM_MODE = "mode";

    public boolean spooler_process() throws Exception {
        String strParamName = "";
        String strMD5FilenameExtension = FILENAME_EXTENSION_MD5;
        String fileName = "";
        String mode = MODE_CREATE;
        try {
            getLogger().debug(VersionInfo.VERSION_STRING);
            // Job oder Order
            Variable_set params = spooler.create_variable_set();
            if (spooler_task.params() != null)
                params.merge(spooler_task.params());
            if (spooler_job.order_queue() != null && spooler_task.order().params() != null)
                params.merge(spooler_task.order().params());
            // mandatory parameters
            strParamName = JOB_PARAMETER_FILE;
            if (params.var(strParamName) != null && params.var(strParamName).length() > 0) {
                fileName = params.var(strParamName);
                // To make orderparams available for substitution in orderparam
                // value
                while (fileName.matches("^.*%[^%]+%.*$")) {
                    String p = fileName.replaceFirst("^.*%([^%]+)%.*$", "$1");
                    String s = params.var(p);
                    s = s.replace('\\', '/');
                    fileName = fileName.replaceAll("%" + p + "%", s);
                    getLogger().debug("processing job parameter [" + strParamName + "]: substitute %" + p + "% with " + s);
                }
            } else {
                throw new Exception("job parameter is missing: [" + strParamName + "]");
            }
            getLogger().info(".. job parameter [" + strParamName + "]: " + fileName);
            strParamName = JOB_PARAM_MD5_SUFFIX;
            if (params.var(strParamName) != null && !params.var(strParamName).isEmpty()) {
                strMD5FilenameExtension = params.var(strParamName);
                getLogger().info(".. job parameter [" + strParamName + "]: " + strMD5FilenameExtension);
            }
            strParamName = JOB_PARAM_MODE;
            if (params.var(strParamName) != null && params.var(strParamName).length() > 0) {
                mode = params.var(strParamName);
                getLogger().info(".. job parameter [" + strParamName + "]: " + mode);
            }
            File file = new File(fileName);
            if (!file.canRead()) {
                getLogger().warn(String.format("Failed to read file: '%1$s'", file.getAbsolutePath()));
                return false;
            }
            File md5File = new File(file.getAbsolutePath() + strMD5FilenameExtension);
            String strFileMD5 = SOSCrypt.MD5encrypt(file);
            getLogger().info("md5 of " + file.getAbsolutePath() + ": " + strFileMD5);
            if (mode.equalsIgnoreCase(MODE_CREATE)) {
                getLogger().debug1("creating md5 file: " + md5File.getAbsolutePath());
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(md5File)));
                out.write(strFileMD5);
                out.close();
            } else {
                getLogger().debug1("checking md5 file: " + md5File.getAbsolutePath());
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(md5File)));
                String strMD5fromFile = in.readLine();
                in.close();
                if (strMD5fromFile != null) {
                    // get only 1st part in case of md5sum format
                    strMD5fromFile = strMD5fromFile.split("\\s+")[0];
                } else
                    strMD5fromFile = "";
                getLogger().debug3("md5 from " + md5File.getAbsolutePath() + ": " + strMD5fromFile);
                if (strMD5fromFile.equalsIgnoreCase(strFileMD5)) {
                    getLogger().info("md5 checksums are equal.");
                } else {
                    getLogger().warn("md5 checksums are different.");
                    return false;
                }
            }
            return (spooler_job.order_queue() != null);
        } catch (Exception e) {
            try {
                getLogger().error("error occurred in JobSchedulerCreateMD5File: " + e.getMessage());
            } catch (Exception x) {
            }
            return false;
        }
    }
}
