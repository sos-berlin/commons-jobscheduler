package sos.scheduler.job;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.net.ssh.SOSSSHJobOptions;
import sos.net.ssh.exceptions.SSHExecutionError;
import sos.scheduler.job.impl.SOSSSHReadPidFileJob;

public class SOSSSHReadPidFileJobJSAdapter extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHReadPidFileJobJSAdapter.class);

    private static final String PID_FILE_NAME_KEY = "job_ssh_pid_file_name";
    private HashMap<String, String> allParams;

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            LOGGER.error(stackTrace2String(e));
            throw new JobSchedulerException(e);
        }
        return signalSuccess();
    }

    private void doProcessing() throws Exception {
        allParams = getGlobalSchedulerParameters();
        allParams.putAll(getParameters());
        SOSSSHJobOptions options = null;
        try {
            SOSSSHReadPidFileJob job = new SOSSSHReadPidFileJob();
            job.setJSJobUtilites(this);
            job.setTempPidFileName(allParams.get(PID_FILE_NAME_KEY));

            options = job.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName(false));
            HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(allParams);
            options.setAllOptions(options.deletePrefix(hsmParameters1, "ssh_"));
            options.checkMandatory();

            job.execute();
        } catch (Exception e) {
            if (options != null && options.raiseExceptionOnError.value()) {
                if (options.ignoreError.value()) {
                    if (options.ignoreStderr.value()) {
                        LOGGER.debug(stackTrace2String(e));
                    } else {
                        LOGGER.error(stackTrace2String(e));
                        throw new SSHExecutionError("Exception raised: " + e, e);
                    }
                } else {
                    LOGGER.error(stackTrace2String(e), e);
                    throw new SSHExecutionError("Exception raised: " + e, e);
                }
            }
        }
    }

}
