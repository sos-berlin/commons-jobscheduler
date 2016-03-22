package sos.scheduler.managed.db;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;

public class JobSchedulerManagedDBReportJob extends JSToolBox implements JSJobUtilities {

    protected JobSchedulerManagedDBReportJobOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerManagedDBReportJob.class);
    private JSJobUtilities objJSJobUtilities = this;

    public JobSchedulerManagedDBReportJob() {
        super();
    }

    public JobSchedulerManagedDBReportJobOptions Options() {
        if (objOptions == null) {
            objOptions = new JobSchedulerManagedDBReportJobOptions();
        }
        return objOptions;
    }

    public JobSchedulerManagedDBReportJobOptions Options(final JobSchedulerManagedDBReportJobOptions pobjOptions) {
        objOptions = pobjOptions;
        return objOptions;
    }

    public JobSchedulerManagedDBReportJob Execute() throws Exception {
        final String conMethodName = "JobSchedulerManagedDBReportJob::Execute";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        try {
            Options().CheckMandatory();
            LOGGER.debug(Options().toString());
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.getMsg("JSJ-I-107"), conMethodName), e);
        } finally {
            LOGGER.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName));
        }
        return this;
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    }

    @Override
    public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
        LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
        return pstrString2Modify;
    }

    @Override
    public void setJSParam(final String pstrKey, final String pstrValue) {

    }

    @Override
    public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {

    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        LOGGER.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

    @Override
    public String getCurrentNodeName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setStateText(final String pstrStateText) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCC(final int pintCC) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setNextNodeState(final String pstrNodeName) {
        // TODO Auto-generated method stub

    }

}