package sos.scheduler.managed.db;

import java.util.HashMap;

import sos.scheduler.managed.db.JobSchedulerManagedDBReportJob;
import sos.scheduler.managed.db.JobSchedulerManagedDBReportJobOptions;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler
                                                 // Java-API-Jobs
import org.apache.log4j.Logger;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.*;

/** \class JobSchedulerManagedDBReportJobJSAdapterClass - JobScheduler Adapter
 * for "Launch Database Report"
 *
 * \brief AdapterClass of JobSchedulerManagedDBReportJob for the SOSJobScheduler
 *
 * This Class JobSchedulerManagedDBReportJobJSAdapterClass works as an
 * adapter-class between the SOS JobScheduler and the worker-class
 * JobSchedulerManagedDBReportJob.
 *
 * 
 *
 * see \see
 * R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc
 * \JobSchedulerManagedDBReportJob.xml for more details.
 *
 * \verbatim ; mechanicaly created by
 * C:\Users\KB\eclipse\xsl\JSJobDoc2JSAdapterClass.xsl from
 * http://www.sos-berlin.com at 20120830214330 \endverbatim */
public class JobSchedulerManagedDBReportJobJSAdapterClass extends JobSchedulerJobAdapter {

    private final String conClassName = "JobSchedulerManagedDBReportJobJSAdapterClass";  //$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerManagedDBReportJobJSAdapterClass.class);

    public void init() {
        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
        doInitialize();
    }

    private void doInitialize() {
    } // doInitialize

    @Override
    public boolean spooler_init() {
        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::spooler_init"; //$NON-NLS-1$
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$

        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            return false;
        } finally {
        } // finally
          // return value for classic and order driven processing
          // TODO create method in base-class for this functionality
        return (spooler_task.job().order_queue() != null);

    } // spooler_process

    @Override
    public void spooler_exit() {
        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::spooler_exit"; //$NON-NLS-1$
        super.spooler_exit();
    }

    private void doProcessing() throws Exception {
        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::doProcessing"; //$NON-NLS-1$

        JobSchedulerManagedDBReportJob objR = new JobSchedulerManagedDBReportJob();
        JobSchedulerManagedDBReportJobOptions objO = objR.Options();
        objO.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
        objO.CheckMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    } // doProcessing

}
