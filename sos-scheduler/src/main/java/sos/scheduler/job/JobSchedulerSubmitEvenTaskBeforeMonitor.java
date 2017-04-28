package sos.scheduler.job;



public class JobSchedulerSubmitEvenTaskBeforeMonitor extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_task_before() throws Exception {

        try {
            JobSchedulerSubmitEventJob.processEvent(spooler, spooler_job, spooler_task, spooler_log);
            return true;
        } catch (Exception e) {
            spooler_log.warn("Error occured in event monitor spooler_task_before: " + e);
            return false;
        }
    }
}
