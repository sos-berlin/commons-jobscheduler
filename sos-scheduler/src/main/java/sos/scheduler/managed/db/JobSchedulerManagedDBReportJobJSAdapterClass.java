package sos.scheduler.managed.db;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class JobSchedulerManagedDBReportJobJSAdapterClass extends JobSchedulerJobAdapter {

    public void init() {
        //
    }

    @Override
    public boolean spooler_init() {
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            return false;
        }
        return (spooler_task.job().order_queue() != null);
    }

    private void doProcessing() throws Exception {
        JobSchedulerManagedDBReportJob objR = new JobSchedulerManagedDBReportJob();
        JobSchedulerManagedDBReportJobOptions objO = objR.Options();
        objO.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
        objO.CheckMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    }

}