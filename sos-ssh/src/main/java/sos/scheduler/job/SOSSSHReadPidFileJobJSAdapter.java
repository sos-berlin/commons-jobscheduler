package sos.scheduler.job;

import java.util.HashMap;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobOptions;
import sos.net.ssh.exceptions.SSHExecutionError;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSSSHReadPidFileJobJSAdapter extends SOSSSHJob2JSBaseAdapter {

    private static final String PID_FILE_NAME_KEY = "job_ssh_pid_file_name";
    private Variable_set allParams;

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            logger.fatal(stackTrace2String(e));
            throw new JobSchedulerException(e);
        }
        return signalSuccess();
    }

    private void doProcessing() throws Exception {
        allParams = getGlobalSchedulerParameters();
        allParams.merge(getParameters());
        SOSSSHJobOptions options = null;
        try {
            SOSSSHJob2 sshJob = new SOSSSHReadPidFileJob();
            ((SOSSSHReadPidFileJob) sshJob).setTempPidFileName(allParams.value(PID_FILE_NAME_KEY));
            logger.debug("SOSSSHReadPidFileJob instantiated!");
            options = sshJob.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName(false));
            HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(allParams);
            options.setAllOptions(options.deletePrefix(hsmParameters1, "ssh_"));
            sshJob.setJSJobUtilites(this);
            options.checkMandatory();
            sshJob.execute();
        } catch (Exception e) {
            if (options.raiseExceptionOnError.value()) {
                if (options.ignoreError.value()) {
                    if (options.ignoreStderr.value()) {
                        logger.debug(this.stackTrace2String(e));
                    } else {
                        logger.error(this.stackTrace2String(e));
                        throw new SSHExecutionError("Exception raised: " + e, e);
                    }
                } else {
                    logger.error(this.stackTrace2String(e));
                    throw new SSHExecutionError("Exception raised: " + e, e);
                }
            }
        }
    }

}
