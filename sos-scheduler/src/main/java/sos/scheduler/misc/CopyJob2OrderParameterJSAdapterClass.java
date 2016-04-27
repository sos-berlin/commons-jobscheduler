package sos.scheduler.misc;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class CopyJob2OrderParameterJSAdapterClass extends JobSchedulerJobAdapter {

    public void init() {
        //
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
        CopyJob2OrderParameter objR = new CopyJob2OrderParameter();
        CopyJob2OrderParameterOptions objO = objR.Options();
        objO.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
        objO.CheckMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    }

}