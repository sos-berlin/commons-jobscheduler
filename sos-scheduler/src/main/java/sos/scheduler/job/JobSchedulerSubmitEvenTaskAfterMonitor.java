package sos.scheduler.job;



public class JobSchedulerSubmitEvenTaskAfterMonitor extends JobSchedulerJobAdapter {

    @Override
    public void spooler_task_after() throws Exception {

        try {
            JobSchedulerSubmitEventJob.processEvent(spooler, spooler_job, spooler_task, spooler_log);
        } catch (Exception e) {
            spooler_log.warn("Error occured in event monitor spooler_task_after: " + e);
        }
    }
}
