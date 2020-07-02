package sos.scheduler.job;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSSSHJob2JSBaseAdapter extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHJob2JSBaseAdapter.class);

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
        } catch (Exception e) {
            LOGGER.error(stackTrace2String(e));
            throw new JobSchedulerException(e);
        }
        return signalSuccess();
    }

}