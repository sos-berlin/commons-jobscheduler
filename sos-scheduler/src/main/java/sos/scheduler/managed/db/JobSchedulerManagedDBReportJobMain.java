package sos.scheduler.managed.db;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;

public class JobSchedulerManagedDBReportJobMain extends JSToolBox {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerManagedDBReportJobMain.class);
    protected JobSchedulerManagedDBReportJobOptions objOptions = null;

    public final static void main(String[] pstrArgs) {
        final String conMethodName = "JobSchedulerManagedDBReportJobMain::Main";
        LOGGER.info("JobSchedulerManagedDBReportJob - Main");
        try {
            JobSchedulerManagedDBReportJob objM = new JobSchedulerManagedDBReportJob();
            JobSchedulerManagedDBReportJobOptions objO = objM.Options();
            objO.CommandLineArgs(pstrArgs);
            objM.Execute();
        } catch (Exception e) {
            LOGGER.error(conMethodName + ": " + "Error occured ..." + e.getMessage(), e);
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", conMethodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", conMethodName));
    }

}