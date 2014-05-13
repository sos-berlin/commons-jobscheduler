package sos.scheduler.xsl;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_107;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_111;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.JSHelper.io.Files.JSXMLFile;

/**
 * \class 		JobSchedulerXslTransform - Workerclass for "JobSchedulerXslTransform"
 *
 * \brief AdapterClass of JobSchedulerXslTransform for the SOSJobScheduler
 *
 * This Class JobSchedulerXslTransform is the worker-class.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerXslTransform.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20110815114219
 * \endverbatim
 *
 * \Version $Id$
 */
public class JobSchedulerXslTransform extends JSJobUtilitiesClass <JobSchedulerXslTransformOptions>  {
	private final String						conClassName		= "JobSchedulerXslTransform";
	private static Logger						logger				= Logger.getLogger(JobSchedulerXslTransform.class);
	private final String						conSVNVersion		= "$Id$";

//	protected JobSchedulerXslTransformOptions	objOptions			= null;
//	private JSJobUtilities						objJSJobUtilities	= this;

	protected HashMap<String, String>			hsmParameters		= null;

	/**
	 *
	 * \brief JobSchedulerXslTransform
	 *
	 * \details
	 *
	 */
	public JobSchedulerXslTransform() {
		super(new JobSchedulerXslTransformOptions());
	}

	/**
	 *
	 * \brief Options - returns the JobSchedulerXslTransformationOptionClass
	 *
	 * \details
	 * The JobSchedulerXslTransformationOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *
	 * \return JobSchedulerXslTransformationOptions
	 *
	 */
	@Override
	public JobSchedulerXslTransformOptions Options() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new JobSchedulerXslTransformOptions();
		}
		return objOptions;
	}

	/**
	 *
	 * \brief Execute - Start the Execution of JobSchedulerXslTransform
	 *
	 * \details
	 *
	 * For more details see
	 *
	 * \see JobSchedulerAdapterClass
	 * \see JobSchedulerXslTransformationMain
	 *
	 * \return JobSchedulerXslTransform
	 *
	 * @return
	 */
	public JobSchedulerXslTransform Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

		logger.debug(JSJ_I_110.get(conMethodName));
		logger.info(conSVNVersion);

		try {
			Options().CheckMandatory();
			logger.debug(Options().dirtyString());

			// TODO Parameter für das XSLT reinreichen (alles was mit xslt: beginnt, allerdings ohne das xslt:)
			// TODO EnvironmentCheck als Option einbauen
			JSXMLFile objXMLFile = new JSXMLFile(Options().FileName.Value());
			// requires ant.main ? This is a know bug. I dont't know on which version of xalan it is solved.
//			objXMLFile.EnvironmentCheck();

			// Copy only, with resolve of xincclude tags
			if (Options().XslFileName.IsEmpty() == true) {
				logger.info("no xslt-file specified. copy xml file only");
				String strXML = objXMLFile.getContent();
				JSFile outFile = new JSFile(Options().OutputFileName.Value());
				// TODO charset as option
				outFile.setCharSet4OutputFile("UTF-8");
				outFile.Write(strXML);
				outFile.close();
			}
			else {
				objXMLFile.setParameters(hsmParameters);
				objXMLFile.Transform(new File(Options().XslFileName.Value()), new File(Options().OutputFileName.Value()));
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			throw new JobSchedulerException(JSJ_F_107.get(conMethodName) + ": "+ e.getMessage(), e);
		}
		finally {
		}

		JSJ_I_111.toLog(conMethodName);
		return this;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	public void setParameters(final HashMap<String, String> pobjHshMap) {
		hsmParameters = pobjHshMap;
	}

	private void doInitialize() {
	} // doInitialize

} // class JobSchedulerXslTransform