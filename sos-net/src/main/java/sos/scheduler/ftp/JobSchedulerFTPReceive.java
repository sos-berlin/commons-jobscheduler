package sos.scheduler.ftp;

import com.sos.JSHelper.Basics.VersionInfo;

import sos.configuration.SOSConfiguration;
import sos.net.SOSFileTransfer;
import sos.net.sosftp.SOSFTPCommandReceive;
import sos.scheduler.job.JobSchedulerJob;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSSchedulerLogger;
import sos.util.SOSString;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/** @author Andreas Püschel
 * @author Mürüvet Öksüz */
public class JobSchedulerFTPReceive extends JobSchedulerJob {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerFTPReceive.class);
    private static final String PREFIX_FTP = "ftp_";
    private static final String SETTING_FILE_SPEC = "file_spec";
    private static final String VARNAME_SETBACK = "setback";
    private static final String VARNAME_SETBACK_COUNT = "setback_count";
    private static final String VARNAME_FTP_FILE_PATH = "ftp_file_path";
    private static final String VARNAME_FTP_RESULT_ERROR_MESSAGE = "ftp_result_error_message";
    private static final String VARNAME_FTP_RESULT_FILES = "ftp_result_files";
    private static final String VARNAME_FTP_RESULT_ZERO_BYTE_FILES = "ftp_result_zero_byte_files";
    private static final String VARNAME_FTP_RESULT_FILENAMES = "ftp_result_filenames";
    private static final String VARNAME_FTP_RESULT_FILEPATHS = "ftp_result_filepaths";
    private static final String VARNAME_FTP_CHECK_PARALLEL = "ftp_check_parallel";
    private static final String VARNAME_FILE_SPEC = SETTING_FILE_SPEC;
    private SOSString sosString = new SOSString();
    private boolean flgUseOrderSetBack = true;
    public static final int ERROR_CODE = 300;
    int pollTimeout = 0;
    int pollIntervall = 60;
    int pollMinFiles = 1;
    int iSetbackCount = 1;
    String pollFilesErrorState = "";

    public boolean spooler_process() {
        boolean checkParallel = false;
        boolean parallelTransfer = false;
        String parallelTransferCheckSetback = "00:00:60";
        int parallelTransferCheckRetry = 60;
        Variable_set params = null;
        boolean rc = false;
        boolean isFilePath = false;
        boolean orderSelfDestruct = false;
        Properties schedulerParams = null;
        try {
            try {
                this.setLogger(new SOSSchedulerLogger(spooler_log));
                getLogger().debug(VersionInfo.VERSION_STRING);
                params = getParameters();
                schedulerParams = getSchedulerParameterAsProperties(params);
                checkParallel = sosString.parseToBoolean(sosString.parseToString(schedulerParams.get("check_parallel")));
                parallelTransfer = sosString.parseToBoolean(sosString.parseToString(schedulerParams.get("parallel")));
            } catch (Exception e) {
                rc = false;
                LOGGER.error(e.getMessage(), e);
                throw (new Exception("could not process job parameters: " + e));
            }
            try {
                if (checkParallel) {
                    boolean bSuccess = true;
                    String[] paramNames = sosString.parseToString(spooler.variables().names()).split(";");
                    for (int i = 0; i < paramNames.length; i++) {
                        if (paramNames[i].startsWith("ftp_check_receive_" + normalize(spooler_task.order().id()) + ".")) {
                            if ("0".equals(sosString.parseToString(spooler.var(paramNames[i])))) {
                                String sRetry =
                                        sosString.parseToString(spooler.variables().var("cur_transfer_retry" + normalize(spooler_task.order().id())));
                                int retry = sRetry.isEmpty() ? 0 : Integer.parseInt(sRetry);
                                --retry;
                                spooler.variables().set_var("cur_transfer_retry" + normalize(spooler_task.order().id()), String.valueOf(retry));
                                if (retry == 0) {
                                    getLogger().debug("terminated cause max order setback reached: " + paramNames[i]);
                                    spooler.variables().set_var("terminated_cause_max_order_setback_" + normalize(spooler_task.order().id()), "1");
                                    return false;
                                }
                                getLogger().debug("launch setback: " + parallelTransferCheckRetry + " * " + parallelTransferCheckSetback);
                                spooler_task.order().setback();
                                return false;
                            } else if ("1".equals(sosString.parseToString(spooler.var(paramNames[i])))) {
                                getLogger().debug("successfully terminated: " + paramNames[i]);
                            } else if ("2".equals(sosString.parseToString(spooler.var(paramNames[i])))) {
                                bSuccess = false;
                                getLogger().debug("terminated with error : " + paramNames[i]);
                            }
                        }
                    }
                    return bSuccess;
                } else if (schedulerParams.get("parent_order_id") != null) {
                    String state =
                            spooler.variables().var(
                                    "terminated_cause_max_order_setback_"
                                            + normalize(sosString.parseToString(schedulerParams.get("ftp_parent_order_id"))));
                    if ("1".equals(state)) {
                        return false;
                    }
                }
                if (!sosString.parseToString(schedulerParams.get("file_path")).isEmpty()) {
                    isFilePath = true;
                } else {
                    isFilePath = false;
                }
            } catch (Exception e) {
                rc = false;
                LOGGER.error(e.getMessage(), e);
                throw (new Exception("invalid or insufficient parameters: " + e));
            }
            try {
                Vector<String> filelist = null;
                String remoteDir = sosString.parseToString(schedulerParams.get("remoteDir"));
                if (parallelTransfer && !isFilePath) {
                    schedulerParams.put("skip_transfer", "yes");
                    SOSConfiguration con =
                            new SOSConfiguration(null, schedulerParams, sosString.parseToString(schedulerParams.get("settings")),
                                    sosString.parseToString(schedulerParams.get("profile")), null, new SOSSchedulerLogger(spooler_log));
                    con.checkConfigurationItems();
                    SOSFTPCommandReceive ftpCommand = new SOSFTPCommandReceive(con, new SOSSchedulerLogger(spooler_log));
                    ftpCommand.setSchedulerJob(this);
                    rc = ftpCommand.transfer();
                    filelist = ftpCommand.getFilelist();
                    Iterator<String> iterator = filelist.iterator();
                    if (spooler_job.order_queue() == null) {
                        while (iterator.hasNext()) {
                            String fileName = sosString.parseToString(iterator.next());
                            String fileSpec =
                                    schedulerParams.containsKey(VARNAME_FILE_SPEC) ? sosString.parseToString(schedulerParams.get(VARNAME_FILE_SPEC))
                                            : ".*";
                            Pattern pattern = Pattern.compile(fileSpec, 0);
                            Matcher matcher = pattern.matcher(fileName);
                            if (matcher.find()) {
                                Variable_set newParams = params;
                                newParams.set_var(VARNAME_FTP_FILE_PATH, (remoteDir.endsWith("/") || remoteDir.endsWith("\\") ? remoteDir : remoteDir
                                        + "/")
                                        + fileName);
                                spooler_log.info("launching job for parallel transfer with parameter: ftp_file_path "
                                        + (remoteDir.endsWith("/") || remoteDir.endsWith("\\") ? remoteDir : remoteDir + "/") + fileName);
                                spooler.job(spooler_task.job().name()).start(params);
                            }
                        }
                        return false;
                    } else {
                        while (iterator.hasNext()) {
                            String fileName = (String) iterator.next();
                            String fileSpec =
                                    schedulerParams.containsKey(SETTING_FILE_SPEC) ? sosString.parseToString(schedulerParams.get(SETTING_FILE_SPEC))
                                            : ".*";
                            Pattern pattern = Pattern.compile(fileSpec, 0);
                            Matcher matcher = pattern.matcher(fileName);
                            if (matcher.find()) {
                                Variable_set newParams = spooler.create_variable_set();
                                if (spooler_task.params() != null) {
                                    newParams.merge(params);
                                }
                                newParams.set_var(VARNAME_FTP_FILE_PATH, (remoteDir.endsWith("/") || remoteDir.endsWith("\\") ? remoteDir : remoteDir
                                        + "/")
                                        + fileName);
                                newParams.set_var("ftp_parent_order_id", spooler_task.order().id());
                                newParams.set_var("ftp_order_self_destruct", "1");
                                Order newOrder = spooler.create_order();
                                newOrder.set_state(spooler_task.order().state());
                                newOrder.set_params(newParams);
                                spooler_task.order().job_chain().add_order(newOrder);
                                getLogger().info(
                                        "launching order for parallel transfer with parameter: ftp_file_path "
                                                + (remoteDir.endsWith("/") || remoteDir.endsWith("\\") ? remoteDir : remoteDir + "/") + fileName);
                                spooler.variables().set_var("ftp_order",
                                        normalize(spooler_task.order().id()) + "." + normalize(newOrder.id()) + "." + "0");
                                spooler.variables().set_var(
                                        "ftp_check_receive_" + normalize(spooler_task.order().id()) + "." + normalize(newOrder.id()), "0");
                            }
                        }
                        spooler_task.order().params().set_var(VARNAME_FTP_CHECK_PARALLEL, "yes");
                        spooler_job.set_delay_order_after_setback(1, parallelTransferCheckSetback);
                        spooler_job.set_max_order_setbacks(parallelTransferCheckRetry);
                        spooler_task.order().setback();
                        spooler.variables().set_var("cur_transfer_retry" + normalize(spooler_task.order().id()),
                                String.valueOf(parallelTransferCheckRetry));
                        return false;
                    }
                }
                SOSConfiguration con =
                        new SOSConfiguration(null, schedulerParams, sosString.parseToString(schedulerParams.get("settings")),
                                sosString.parseToString(schedulerParams.get("profile")), null, new SOSSchedulerLogger(spooler_log));
                con.checkConfigurationItems();
                sos.net.sosftp.SOSFTPCommandReceive ftpCommand = new sos.net.sosftp.SOSFTPCommandReceive(con, new SOSSchedulerLogger(spooler_log));
                ftpCommand.setSchedulerJob(this);
                rc = ftpCommand.transfer();
                createOrderParameter(ftpCommand);
                if (parallelTransfer && isFilePath && spooler_job.order_queue() != null) {
                    spooler.variables().set_var(
                            "ftp_check_receive_" + normalize(params.var("ftp_parent_order_id")) + "." + normalize(spooler_task.order().id()), "1");
                }
                processResult(rc, "");
                spooler_job.set_state_text(ftpCommand.getState() != null ? ftpCommand.getState() : "");
                return spooler_task.job().order_queue() == null ? false : rc;
            } catch (Exception e) {
                rc = false;
                if (parallelTransfer && isFilePath && spooler_job.order_queue() != null) {
                    spooler.variables().set_var(
                            "ftp_check_receive_" + normalize(normalize(params.var("ftp_parent_order_id"))) + "."
                                    + normalize(spooler_task.order().id()), "2");
                }
                spooler_job.set_state_text("could not process file transfer: " + e);
                throw (new Exception("could not process file transfer: " + e, e));
            } finally {
                if (parallelTransfer && orderSelfDestruct) {
                    String state = "";
                    sos.spooler.Job_chain_node node = spooler_task.order().job_chain_node();
                    while (node != null) {
                        node = node.next_node();
                        if (node != null) {
                            state = node.state();
                        }
                    }
                    spooler_task.order().set_state(state);
                }
            }
        } catch (Exception e) {
            processResult(false, e.toString());
            spooler_log.warn("ftp processing failed: " + e.toString());
            if (spooler_job.order_queue() != null && spooler_task.order() != null && spooler_task.order().params() != null) {
                spooler_task.order().params().set_var(VARNAME_SETBACK_COUNT, "");
            }
            return false;
        }
    }

    private String normalize(String str) {
        return str.replaceAll(",", "_");
    }

    protected void processResult(boolean rc, String message) {
        // do nothing, entry point for subclasses
    }

    private Variable_set getParameters() throws Exception {
        try {
            Variable_set params = spooler.create_variable_set();
            if (spooler_task.params() != null) {
                params.merge(spooler_task.params());
            }
            if (spooler_job.order_queue() != null && spooler_task.order().params() != null) {
                params.merge(spooler_task.order().params());
                Variable_set orderParams = spooler_task.order().params();
                String setbackCount = orderParams.value(VARNAME_SETBACK_COUNT);
                getLogger().debug9("setback_count read: " + setbackCount);
                if (setbackCount != null && !setbackCount.isEmpty()) {
                    iSetbackCount = Integer.parseInt(setbackCount);
                    iSetbackCount++;
                }
                orderParams.set_var(VARNAME_SETBACK_COUNT, "" + iSetbackCount);
            }
            return params;
        } catch (Exception e) {
            throw new Exception("error occurred reading Parameter: " + e.getMessage());
        }
    }

    private Properties getSchedulerParameterAsProperties(Variable_set params) throws Exception {
        Properties schedulerParams = new Properties();
        try {
            if (params == null) {
                return new Properties();
            }
            String[] names = params.names().split(";");
            getLogger().debug9("names " + params.names());
            for (int i = 0; i < names.length; i++) {
                String key = names[i];
                String val = params.var(names[i]);
                if (key.startsWith(PREFIX_FTP) && key.length() > PREFIX_FTP.length()) {
                    key = key.substring(PREFIX_FTP.length());
                }
                if (key.contains("password")) {
                    getLogger().debug("param [" + key + "=*****]");
                } else {
                    getLogger().debug("param [" + key + "=" + val + "]");
                }
                schedulerParams.put(key, val);
            }
            if (!sosString.parseToString(schedulerParams.get("use_order_set_back")).isEmpty()) {
                flgUseOrderSetBack = sosString.parseToBoolean(schedulerParams.get("use_order_set_back"));
            }
            schedulerParams.put("operation", "receive");
            try {
                schedulerParams.put("mail_smtp", spooler_log.mail().smtp());
                schedulerParams.put("mail_queue_dir", spooler_log.mail().queue_dir());
                schedulerParams.put("mail_from", spooler_log.mail().from());
            } catch (Exception e) {
                schedulerParams.put("mail_smtp", "localhost");
                schedulerParams.put("mail_queue_dir", "");
                schedulerParams.put("mail_from", "SOSFTP");
            }
            String fileNotificationTo = sosString.parseToString(schedulerParams.get("file_notification_to"));
            String fileNotificationSubject = sosString.parseToString(schedulerParams.get("file_notification_subject"));
            String fileNotificationBody = sosString.parseToString(schedulerParams.get("file_notification_body"));
            if (fileNotificationTo != null && !fileNotificationTo.isEmpty()) {
                if (fileNotificationSubject == null || fileNotificationSubject.isEmpty()) {
                    if (spooler_job.order_queue() != null) {
                        fileNotificationSubject =
                                "[info] Job Chain: " + spooler_task.order().job_chain().name() + ", Order: " + spooler_task.order().id() + ", Job: "
                                        + spooler_job.name() + " (" + spooler_job.title() + "), Task: " + spooler_task.id();
                    } else {
                        fileNotificationSubject = "[info] Job: " + spooler_job.name() + " (" + spooler_job.title() + "), Task: " + spooler_task.id();
                    }
                    schedulerParams.put("file_notification_subject", fileNotificationSubject);
                }
                if (fileNotificationBody == null || fileNotificationBody.isEmpty()) {
                    fileNotificationBody = "The following files have been received:\n\n";
                    schedulerParams.put("file_notification_body", fileNotificationBody);
                }
            }
            String fileZeroByteNotificationTo = sosString.parseToString(schedulerParams.get("file_zero_byte_notification_to"));
            String fileZeroByteNotificationSubject = sosString.parseToString(schedulerParams.get("file_zero_byte_notification_subject"));
            String fileZeroByteNotificationBody = sosString.parseToString(schedulerParams.get("file_zero_byte_notification_body"));
            if (fileZeroByteNotificationTo != null && !fileZeroByteNotificationTo.isEmpty()) {
                if (fileZeroByteNotificationSubject == null || fileZeroByteNotificationSubject.isEmpty()) {
                    if (spooler_job.order_queue() != null) {
                        fileZeroByteNotificationSubject =
                                "[warning] Job Chain: " + spooler_task.order().job_chain().name() + ", Order: " + spooler_task.order().id()
                                        + ", Job: " + spooler_job.name() + " (" + spooler_job.title() + "), Task: " + spooler_task.id();
                    } else {
                        fileZeroByteNotificationSubject =
                                "[warning] Job: " + spooler_job.name() + " (" + spooler_job.title() + "), Task: " + spooler_task.id();
                    }
                    schedulerParams.put("file_zero_byte_notification_subject", fileZeroByteNotificationSubject);
                }
                if (fileZeroByteNotificationBody == null || fileZeroByteNotificationBody.isEmpty()) {
                    fileZeroByteNotificationBody = "The following files have been received and were removed due to zero byte constraints:\n\n";
                    schedulerParams.put("file_zero_byte_notification_body", fileZeroByteNotificationBody);
                }
            }
            return schedulerParams;
        } catch (Exception e) {
            throw new Exception("error occurred reading Parameter: " + e.getMessage());
        }
    }

    private void createOrderParameter(SOSFTPCommandReceive ftpCommand) throws Exception {
        try {
            String fileNames = "";
            String filePaths = "";
            Variable_set objParams = null;
            if (spooler_job.order_queue() != null) {
                if (spooler_task.order() != null && spooler_task.order().params() != null) {
                    objParams = spooler_task.order().params();
                }
            } else {
                objParams = spooler_task.params();
            }
            if (objParams != null) {
                Vector<File> transfFiles = ftpCommand.getTransferredFilelist();
                if (!transfFiles.isEmpty()) {
                    for (File curFile : transfFiles) {
                        filePaths += curFile.getAbsolutePath() + ";";
                        fileNames += curFile.getName() + ";";
                    }
                    filePaths = filePaths.substring(0, filePaths.length() - 1);
                    fileNames = fileNames.substring(0, fileNames.length() - 1);
                }
                int count = ftpCommand.getOfTransferFilesCount();
                objParams.set_var(VARNAME_FTP_RESULT_FILES, Integer.toString(count));
                objParams.set_var(VARNAME_FTP_RESULT_ZERO_BYTE_FILES, Integer.toString(ftpCommand.getZeroByteCount()));
                objParams.set_var(VARNAME_FTP_RESULT_FILENAMES, fileNames);
                objParams.set_var(VARNAME_FTP_RESULT_FILEPATHS, filePaths);
                objParams.set_var(VARNAME_SETBACK_COUNT, "");
            }
        } catch (Exception e) {
            throw new Exception("error occurred creating order Parameter: " + e.getMessage());
        }
    }

    public boolean polling(Vector<String> filelist, boolean isFilePath, String filePath, SOSFileTransfer ftpClient, String fileSpec,
            boolean recursive, boolean forceFiles, int pollTimeout, int pollIntervall, int pollMinFiles, String pollFilesErrorState1)
            throws Exception {
        double delay = pollIntervall;
        getLogger().debug("calling: " + sos.util.SOSClassUtil.getMethodName());
        if (pollTimeout > 0) {
            boolean flgStopPolling = false;
            boolean giveUpPoll = false;
            Iterator<String> iterator = filelist.iterator();
            double nrOfTries = (pollTimeout * 60) / delay;
            int tries = 0;
            while (!flgStopPolling && !giveUpPoll) {
                tries++;
                int matchedFiles = 0;
                while (iterator.hasNext()) {
                    String fileName = (String) iterator.next();
                    File file = new File(fileName);
                    String strFileName4Matcher = file.getName();
                    boolean found = false;
                    if (isFilePath) {
                        try {
                            long si = ftpClient.size(strFileName4Matcher);
                            if (si > -1) {
                                found = true;
                            }
                        } catch (Exception e) {
                            getLogger().debug9("File " + fileName + " not found.");
                        }
                        if (found) {
                            matchedFiles++;
                            getLogger().debug8("Found matching file " + fileName);
                        }
                    } else {
                        Pattern pattern = Pattern.compile(fileSpec, 0);
                        Matcher matcher = pattern.matcher(strFileName4Matcher);
                        if (matcher.find()) {
                            matchedFiles++;
                            getLogger().debug8("Found matching file " + fileName);
                        }
                    }
                }
                getLogger().debug3(matchedFiles + " matching files found");
                if (matchedFiles < pollMinFiles) {
                    if (flgUseOrderSetBack && (spooler_job.order_queue() != null && spooler_task.order() != null)) {
                        iSetbackCount = spooler_task.order().setback_count();
                        flgStopPolling = true;
                        Variable_set orderParams = spooler_task.order().params();
                        getLogger().info("setback_count is now: " + iSetbackCount + " , maximum number of setbacks: " + nrOfTries);
                        if (iSetbackCount >= nrOfTries) {
                            orderParams.set_var(VARNAME_SETBACK_COUNT, "");
                            getLogger().info("give up polling due to max setbacks reached");
                            giveUpPoll = true;
                        } else {
                            getLogger().info(matchedFiles + " matching files found." + pollMinFiles + " files required, setting back order.");
                            spooler_job.set_delay_order_after_setback(1, delay);
                            spooler_job.set_max_order_setbacks((int) nrOfTries);
                            spooler_task.order().setback();
                            iSetbackCount++;
                            return false;
                        }
                    } else {
                        if (tries < nrOfTries) {
                            Thread.sleep((long) delay * 1000);
                            spooler_job.set_state_text("Polling for files... ");
                            if (isFilePath) {
                                filelist = new Vector<String>();
                                filelist.add(filePath);
                            } else {
                                filelist = ftpClient.nList(recursive);
                            }
                            for (int i = 0; i < filelist.size(); i++) {
                                getLogger().debug9(i + " filelist 2 -> " + filelist.get(i));
                            }
                            iterator = filelist.iterator();
                        } else {
                            giveUpPoll = true;
                        }
                    }
                } else {
                    flgStopPolling = true;
                    spooler_job.set_state_text("");
                }
                if (giveUpPoll) {
                    spooler_task.order().params().set_var(VARNAME_SETBACK, "false");
                    String message = "Failed to find at least " + pollMinFiles + " files matching \"" + fileSpec + "\" ";
                    if (isFilePath) {
                        message = "Failed to find file \"" + filePath + "\" ";
                    }
                    message += "after triggering for " + pollTimeout + " minutes.";
                    getLogger().debug(message);
                    if (matchedFiles > 0) {
                        message += " (only " + matchedFiles + " files found)";
                    }
                    if (pollFilesErrorState1 != null && !pollFilesErrorState1.isEmpty()) {
                        getLogger().debug("set order-state to " + pollFilesErrorState1);
                        spooler_task.order().set_state(pollFilesErrorState1);
                        spooler_task.order().params().set_var(VARNAME_FTP_RESULT_ERROR_MESSAGE, message);
                    }
                    if (forceFiles) {
                        spooler_log.warn(message);
                        String body = message + "\n";
                        body += "See attached logfile for details.";
                        spooler_log.mail().set_body(body);
                        spooler_task.end();
                    } else {
                        spooler_log.info(message);
                        return true;
                    }
                    return false;
                }
            }
        }
        return true;
    }

}