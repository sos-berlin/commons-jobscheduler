package sos.scheduler.reports;
import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class 		JSReportAllParametersJSAdapterClass - JobScheduler Adapter for "Report all Parameters"
 *
 * \brief AdapterClass of JSReportAllParameters for the SOSJobScheduler
 *
 * This Class JSReportAllParametersJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JSReportAllParameters.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JSReportAllParameters.xml for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20110516150402 
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JSReportAllParametersJSAdapterClass extends JobSchedulerJobAdapter {
	private final String	conClassName	= "JSReportAllParametersJSAdapterClass";						//$NON-NLS-1$
	private static Logger	logger			= Logger.getLogger(JSReportAllParametersJSAdapterClass.class);
	private final String					conSVNVersion		= "$Id$";

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
		}
		catch (Exception e) {
			return false;
		}
		finally {
		} // finally
		return signalSuccess();
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
		JSReportAllParameters objR = new JSReportAllParameters();
		JSReportAllParametersOptions objO = objR.Options();
		objO.CurrentNodeName(getCurrentNodeName());

		objO.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
		objO.CheckMandatory();
		objR.setJSJobUtilites(this);
		objR.Execute();
	} // doProcessing
}
