package sos.scheduler.job;

import sos.spooler.Monitor_impl;

public class JobSchedulerSubmitEventProcessBeforeMonitor extends Monitor_impl {

    public boolean spooler_process_before() throws Exception {

        try {
            JobSchedulerSubmitEventJob.processEvent(spooler, spooler_job, spooler_task, spooler_log);
            return true;
        } catch (Exception e) {
            spooler_log.warn("Error occured in event monitor spooler_process_before: " + e);
            return false;
        }
    }

}
