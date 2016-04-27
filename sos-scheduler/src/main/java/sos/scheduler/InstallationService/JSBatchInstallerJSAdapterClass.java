package sos.scheduler.InstallationService;

import java.util.HashMap;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Variable_set;

public class JSBatchInstallerJSAdapterClass extends JobSchedulerJobAdapter {

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

    @Override
    public void spooler_exit() {
        super.spooler_exit();
    }

    private void doProcessing() throws Exception {
        JSBatchInstaller objR = new JSBatchInstaller();
        JSBatchInstallerOptions objO = objR.Options();
        Variable_set varT = getParameters();
        HashMap<String, String> hshT = null;
        hshT = getSchedulerParameterAsProperties(varT);
        objO.setAllOptions(hshT);
        objO.CheckMandatory();
        objR.setJSJobUtilites(this);
        objR.setJSCommands(this);
        objR.Execute();
    }

}