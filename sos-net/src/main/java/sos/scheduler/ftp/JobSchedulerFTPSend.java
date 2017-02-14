package sos.scheduler.ftp;

import com.sos.JSHelper.Basics.VersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.configuration.SOSConfiguration;
import sos.net.sosftp.SOSFTPCommandSend;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSSchedulerLogger;
import sos.util.SOSString;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

/** @author Andreas Püschel
 * @author Mürüvet Öksüz
 * @author oh */
@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class JobSchedulerFTPSend extends JobSchedulerJobAdapter {

    private static final String ORDER_PARAMETER_FTP_RESULT_ZERO_BYTE_FILES = "ftp_result_zero_byte_files";
    private static final String ORDER_PARAMETER_FTP_RESULT_FILES = "ftp_result_files";
    private static final String PARAMETER_PARALLEL = "parallel";
    private static final String PARAMETER_CHECK_PARALLEL = "check_parallel";
    private static final String PARAMETER_PROFILE = "profile";
    private static final String PARAMETER_SETTINGS = "settings";
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerFTPSend.class);
    private SOSString sosString = new SOSString();
    private JSOptionsClass objOptions = new JSOptionsClass();

    public boolean spooler_process() {
        try {
            super.spooler_process();
        } catch (Exception e1) {
            LOGGER.error(e1.getMessage(), e1);
        }
        boolean checkParallel = false;
        boolean parallelTransfer = false;
        String parallelTransferCheckSetback = "00:00:60";
        int parallelTransferCheckRetry = 60;
        Variable_set params = null;
        boolean rc = false;
        boolean isFilePath = false;
        boolean orderSelfDestruct = false;
        HashMap<String, String> schedulerParams = null;
        try {
            spooler_log.info(VersionInfo.VERSION_STRING);
            try {
                params = getParameters();
                schedulerParams = objOptions.deletePrefix(super.getSchedulerParameterAsProperties(params), "ftp_");
                schedulerParams.putAll(getParameterDefaults(params));
                checkParallel = sosString.parseToBoolean(sosString.parseToString(schedulerParams.get(PARAMETER_CHECK_PARALLEL)));
                parallelTransfer = sosString.parseToBoolean(sosString.parseToString(schedulerParams.get(PARAMETER_PARALLEL)));
            } catch (Exception e) {
                throw new JobSchedulerException("could not process job parameters: " + e.getMessage(), e);
            }
            try {
                if (checkParallel && spooler_job.order_queue() != null) {
                    boolean bSuccess = true;
                    String[] paramNames = sosString.parseToString(spooler.variables().names()).split(";");
                    for (int i = 0; i < paramNames.length; i++) {
                        if (paramNames[i].startsWith("ftp_check_send_" + normalize(spooler_task.order().id()) + ".")) {
                            if ("0".equals(sosString.parseToString(spooler.var(paramNames[i])))) {
                                String sRetry =
                                        sosString.parseToString(spooler.variables().var("cur_transfer_retry" + normalize(spooler_task.order().id())));
                                int retry = sRetry.isEmpty() ? 0 : Integer.parseInt(sRetry);
                                --retry;
                                spooler.variables().set_var("cur_transfer_retry" + normalize(spooler_task.order().id()), String.valueOf(retry));
                                if (retry == 0) {
                                    spooler_log.debug("terminated cause max order setback reached: " + paramNames[i]);
                                    spooler.variables().set_var("terminated_cause_max_order_setback_" + normalize(spooler_task.order().id()), "1");
                                    return false;
                                }
                                spooler_log.debug("launching setback: " + parallelTransferCheckRetry + " * " + parallelTransferCheckSetback);
                                spooler_task.order().setback();
                                return false;
                            } else if ("1".equals(sosString.parseToString(spooler.var(paramNames[i])))) {
                                spooler_log.debug("successfully terminated: " + paramNames[i]);
                            } else if ("2".equals(sosString.parseToString(spooler.var(paramNames[i])))) {
                                bSuccess = false;
                                spooler_log.debug("terminated with error : " + paramNames[i]);
                            }
                        }
                    }
                    return bSuccess;
                } else if (sosString.parseToString(params.var("ftp_parent_order_id")).length() > 0) {
                    String state = spooler.variables().var("terminated_cause_max_order_setback_" + normalize(params.var("ftp_parent_order_id")));
                    if ("1".equals(state)) {
                        return false;
                    }
                }
                if (sosString.parseToString(schedulerParams.get("file_path")).length() > 0) {
                    isFilePath = true;
                } else {
                    isFilePath = false;
                }
            } catch (Exception e) {
                throw new Exception("invalid or insufficient parameters: " + e.getMessage());
            }
            try {
                if (parallelTransfer && !isFilePath) {
                    Properties p = new Properties();
                    p.putAll((Properties) schedulerParams.clone());
                    p.put("skip_transfer", "yes");
                    SOSConfiguration con =
                            new SOSConfiguration(null, p, sosString.parseToString(schedulerParams.get(PARAMETER_SETTINGS)),
                                    sosString.parseToString(schedulerParams.get(PARAMETER_PROFILE)), null);
                    con.checkConfigurationItems();
                    sos.net.sosftp.SOSFTPCommandSend ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(con, new SOSSchedulerLogger(spooler_log));
                    ftpCommand.setSchedulerJob(this);
                    rc = ftpCommand.transfer();
                    Vector<File> filelist = ftpCommand.getTransferredFilelist();
                    Iterator iterator = filelist.iterator();
                    if (!isJobchain()) {
                        while (iterator.hasNext()) {
                            File fileName = (File) iterator.next();
                            Variable_set newParams = params;
                            newParams.set_var("ftp_file_path", fileName.getCanonicalPath());
                            newParams.set_var("ftp_local_dir", "");
                            spooler_log.info("launching job for parallel transfer with parameter ftp_file_path: " + fileName.getCanonicalPath());
                            spooler.job(spooler_task.job().name()).start(params);
                        }
                        return signalSuccess();
                    } else {
                        while (iterator.hasNext()) {
                            File fileName = (File) iterator.next();
                            Variable_set newParams = spooler.create_variable_set();
                            if (spooler_task.params() != null) {
                                newParams.merge(params);
                            }
                            newParams.set_var("ftp_file_path", fileName.getCanonicalPath());
                            newParams.set_var("ftp_parent_order_id", spooler_task.order().id());
                            newParams.set_var("ftp_order_self_destruct", "1");
                            Order newOrder = spooler.create_order();
                            newOrder.set_state(spooler_task.order().state());
                            newOrder.set_params(newParams);
                            spooler.job_chain(spooler_task.order().job_chain().name()).add_order(newOrder);
                            spooler_log.info("launching order for parallel transfer with parameter ftp_file_path: " + fileName.getCanonicalPath());
                            spooler.variables().set_var("ftp_order",
                                    normalize(spooler_task.order().id()) + "." + normalize(newOrder.id()) + "." + "0");
                            spooler.variables().set_var("ftp_check_send_" + normalize(spooler_task.order().id()) + "." + normalize(newOrder.id()),
                                    "0");

                        }
                        spooler_task.order().params().set_var("ftp_check_parallel", "yes");
                        spooler_job.set_delay_order_after_setback(1, parallelTransferCheckSetback);
                        spooler_job.set_max_order_setbacks(parallelTransferCheckRetry);
                        spooler_task.order().setback();
                        spooler.variables().set_var("cur_transfer_retry" + normalize(spooler_task.order().id()),
                                String.valueOf(parallelTransferCheckRetry));
                        return false;
                    }
                }
                SOSConfiguration con =
                        new SOSConfiguration(null, mapToProperties(schedulerParams), sosString.parseToString(schedulerParams.get(PARAMETER_SETTINGS)),
                                sosString.parseToString(schedulerParams.get(PARAMETER_PROFILE)), null);
                con.checkConfigurationItems();
                sos.net.sosftp.SOSFTPCommandSend ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(con, new SOSSchedulerLogger(spooler_log));
                ftpCommand.setSchedulerJob(this);
                rc = ftpCommand.transfer();
                createReturnParameter(ftpCommand);
                if (parallelTransfer && isFilePath && spooler_job.order_queue() != null) {
                    spooler.variables().set_var(
                            "ftp_check_send_" + normalize(params.var("ftp_parent_order_id")) + "." + normalize(spooler_task.order().id()), "1");
                }
                processResult(rc, "");
                spooler_job.set_state_text(ftpCommand.getState() != null ? ftpCommand.getState() : "");
                return (spooler_task.job().order_queue() == null) ? false : rc;
            } catch (Exception e) {
                rc = false;
                if (parallelTransfer && isFilePath && spooler_job.order_queue() != null) {
                    spooler.variables().set_var(
                            "ftp_check_send_" + normalize(params.var("ftp_parent_order_id")) + "." + normalize(spooler_task.order().id()), "2");
                }
                throw new Exception("could not process file transfer: " + e, e);
            } finally {
                if (parallelTransfer) {
                    if (orderSelfDestruct) {
                        String state = "";
                        sos.spooler.Job_chain_node node = spooler_task.order().job_chain_node();
                        while (node != null) {
                            node = node.next_node();
                            if (node != null) {
                                state = node.state();
                            }
                        }
                        spooler_log.debug9("..set state for parallel order job: " + state);
                        spooler_task.order().set_state(state);
                    }
                }
            }
        } catch (Exception e) {
            processResult(false, e.toString());
            spooler_job.set_state_text("ftp processing failed: " + e);
            spooler_log.warn("ftp processing failed: " + e);
            return false;
        }
    }

    protected void processResult(boolean rc, String message) {
        // do nothing, entry point for subclasses
    }

    private String normalize(String str) {
        return str.replaceAll(",", "_");
    }

    private HashMap<String, String> getParameterDefaults(Variable_set params) throws Exception {
        HashMap<String, String> schedulerParams = new HashMap<String, String>();
        try {
            schedulerParams.put("operation", "send");
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
                    fileNotificationBody = "The following files have been send:\n\n";
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
                    fileZeroByteNotificationBody = "The following files have been send and were removed due to zero byte constraints:\n\n";
                    schedulerParams.put("file_zero_byte_notification_body", fileZeroByteNotificationBody);
                }
            }
            return schedulerParams;
        } catch (Exception e) {
            throw new Exception("error occurred reading Parameter: " + e.getMessage());
        }
    }

    private void createReturnParameter(SOSFTPCommandSend ftpCommand) throws Exception {
        try {
            int count = ftpCommand.getOfTransferFilesCount();
            int zeroByteCount = ftpCommand.getZeroByteCount();
            if (isJobchain()) {
                spooler_task.order().params().set_var(ORDER_PARAMETER_FTP_RESULT_FILES, Integer.toString(count));
                spooler_task.order().params().set_var(ORDER_PARAMETER_FTP_RESULT_ZERO_BYTE_FILES, Integer.toString(zeroByteCount));
            } else {
                spooler_task.params().set_var(ORDER_PARAMETER_FTP_RESULT_FILES, Integer.toString(count));
                spooler_task.params().set_var(ORDER_PARAMETER_FTP_RESULT_ZERO_BYTE_FILES, Integer.toString(zeroByteCount));
            }
        } catch (Exception e) {
            throw new Exception("error occurred creating order Patameter: " + e.getMessage());
        }
    }

}