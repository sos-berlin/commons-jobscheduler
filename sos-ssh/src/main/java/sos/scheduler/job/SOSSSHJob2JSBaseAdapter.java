package sos.scheduler.job;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSSSHJob2JSBaseAdapter extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = Logger.getLogger(SOSSSHJob2JSBaseAdapter.class);

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
        } catch (Exception e) {
            LOGGER.fatal(stackTrace2String(e));
            throw new JobSchedulerException(e);
        }
        return signalSuccess();
    }

}