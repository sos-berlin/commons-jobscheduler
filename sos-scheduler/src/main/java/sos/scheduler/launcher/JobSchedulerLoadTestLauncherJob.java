package sos.scheduler.launcher;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.spooler.Job_impl;
import sos.spooler.Variable_set;
import sos.util.SOSClassUtil;
import sos.util.SOSString;

/** @author M�r�vet �ks�z */
public class JobSchedulerLoadTestLauncherJob extends Job_impl {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerLoadTestLauncherJob.class);
    private SOSString sosString = null;

    public boolean spooler_init() {
        try {
            sosString = new SOSString();
            return true;
        } catch (Exception e) {
            spooler_log.error("error occurred initializing job: " + e.getMessage());
            return false;
        }
    }

    public boolean spooler_process() {
        Variable_set parameters = null;
        HashMap allParams = null;
        JobSchedulerLoadTestLauncher launcher = null;
        try {
            spooler_log.debug3(".. calling " + SOSClassUtil.getMethodName());
            parameters = spooler_task.params();
            if (spooler_job.order_queue() != null) {
                parameters.merge(spooler_task.order().params());
            }
            allParams = this.getParameters();
            launcher = new JobSchedulerLoadTestLauncher();
            launcher.setParameters(allParams);
            launcher.process();
            spooler_job.set_state_text(launcher.getStateText());
            return spooler_job.order_queue() != null;
        } catch (Exception e) {
            spooler_log.error("error occurred in execution: " + e.getMessage());
            return false;
        }
    }

    public HashMap getParameters() throws Exception {
        Variable_set parameters = null;
        String[] names = null;
        HashMap allParam = new HashMap();
        try {
            spooler_log.debug3(".. calling " + SOSClassUtil.getMethodName());
            parameters = spooler_task.params();
            if (parameters.count() > 0) {
                names = parameters.names().split(";");
                for (int i = 0; i < parameters.count(); i++) {
                    if (!sosString.parseToString(parameters.var(names[i])).isEmpty()) {
                        allParam.put(names[i], parameters.var(names[i]));
                    }
                }
            }
            return allParam;
        } catch (Exception e) {
            throw new Exception("error occurred processing task parameters: " + e.getMessage());
        }
    }

}