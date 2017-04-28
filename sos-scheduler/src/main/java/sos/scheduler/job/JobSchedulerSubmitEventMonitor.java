package sos.scheduler.job;

import sos.spooler.Monitor_impl;

public class JobSchedulerSubmitEventMonitor extends Monitor_impl {

    public boolean spooler_process_after(boolean result) throws Exception {

        try {
            JobSchedulerSubmitEventJob.processEvent(spooler, spooler_job, spooler_task, spooler_log);
        } catch (Exception e) {
            spooler_log.warn("Error occured in event monitor spooler_process_after: " + e);
        }

        return result;
    }

}
