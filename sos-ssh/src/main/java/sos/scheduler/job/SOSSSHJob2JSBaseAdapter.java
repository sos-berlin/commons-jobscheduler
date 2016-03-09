package sos.scheduler.job;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSSSHJob2JSBaseAdapter extends JobSchedulerJobAdapter {

    private final String conClassName = this.getClass().getSimpleName();
    protected final Logger logger = Logger.getLogger(this.getClass());

    public void init() throws Exception {
        doInitialize();
    }

    @Override
    public boolean spooler_init() {
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
        } catch (Exception e) {
            logger.fatal(StackTrace2String(e));
            throw new JobSchedulerException(e);
        }
        return signalSuccess();
    }

    @Override
    public void spooler_exit() {
        super.spooler_exit();
    }

    private void doInitialize() throws Exception {
    }

}
