package sos.scheduler.LaunchAndObserve;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sos.net.SOSMail;
import sos.util.SOSStandardLogger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.interfaces.ISOSSmtpMailOptions;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Answer;
import com.sos.scheduler.model.answers.HistoryEntry;
import com.sos.scheduler.model.answers.JSCmdBase;
import com.sos.scheduler.model.answers.Job;
import com.sos.scheduler.model.answers.Log;
import com.sos.scheduler.model.answers.Task;
import com.sos.scheduler.model.commands.JSCmdKillTask;
import com.sos.scheduler.model.commands.JSCmdModifyOrder;
import com.sos.scheduler.model.commands.JSCmdShowHistory;
import com.sos.scheduler.model.commands.JSCmdShowJob;
import com.sos.scheduler.model.commands.JSCmdStartJob;
import com.sos.scheduler.model.objects.Spooler;

/**
 * \class 		JobSchedulerLaunchAndObserve - Workerclass for "Launch and observe any given job or job chain"
 *
 * \brief AdapterClass of JobSchedulerLaunchAndObserve for the SOSJobScheduler
 *
 * This Class JobSchedulerLaunchAndObserve is the worker-class.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerLaunchAndObserve.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20111124184851 
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerLaunchAndObserve extends JSToolBox implements JSJobUtilities {
	private final String							conClassName		= "JobSchedulerLaunchAndObserve";										//$NON-NLS-1$
	private static Logger							logger				= Logger.getLogger(JobSchedulerLaunchAndObserve.class);

	@SuppressWarnings("unused")
	private final String							conSVNVersion		= "$Id: JobSchedulerJobAdapter.java 15749 2011-11-22 16:04:10Z kb $";

	protected JobSchedulerLaunchAndObserveOptions	objOptions			= null;
	private JSJobUtilities							objJSJobUtilities	= this;

	/**
	 * 
	 * \brief JobSchedulerLaunchAndObserve
	 *
	 * \details
	 *
	 */
	public JobSchedulerLaunchAndObserve() {
		super("com_sos_scheduler_messages");
	}

	/**
	 * 
	 * \brief Options - returns the JobSchedulerLaunchAndObserveOptionClass
	 * 
	 * \details
	 * The JobSchedulerLaunchAndObserveOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JobSchedulerLaunchAndObserveOptions
	 *
	 */
	public JobSchedulerLaunchAndObserveOptions Options() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new JobSchedulerLaunchAndObserveOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Options - set the JobSchedulerLaunchAndObserveOptionClass
	 * 
	 * \details
	 * The JobSchedulerLaunchAndObserveOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JobSchedulerLaunchAndObserveOptions
	 *
	 */
	public JobSchedulerLaunchAndObserveOptions Options(final JobSchedulerLaunchAndObserveOptions pobjOptions) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		objOptions = pobjOptions;
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of JobSchedulerLaunchAndObserve
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see JobSchedulerLaunchAndObserveMain
	 * 
	 * \return JobSchedulerLaunchAndObserve
	 *
	 * @return
	 */
	public JobSchedulerLaunchAndObserve Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

		int intLastHeartBeatCount = 0;
		int intLastTaskID = 0;
		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));

		Options().CheckMandatory();
		logger.debug(Options().toString());

		SchedulerObjectFactory objSchedulerObjectFactory = new SchedulerObjectFactory(objOptions.scheduler_host.Value(), objOptions.scheduler_port.value());
		objSchedulerObjectFactory.initMarshaller(Spooler.class);

		/**
		 * \TODO Wenn der Job über eine Order in einer Jobkette gestartet wurde dann auch auf die Order prüfen sonst kann der falsche job erwischt werden
		 * \TODO Wenn es mehrere Tasks für den job gibt dann wie die richtige Task erwischen. evtl. die task beim start abfragen und merken
		 */
		try {

			boolean flgIsJobRunning = false;

			String strJobName = objOptions.job_name.Value();
			String strJobChainName = objOptions.order_jobchain_name.Value();
			String strOrderName = objOptions.OrderId.Value();

			String strRegExp = objOptions.check_for_regexp.Value(); // "Antwort von";
			int intTimeBetweenCheck = objOptions.check_interval.value();
			int intMaxNoOfRestarts = 9999;

			boolean flgCheckInactivity = isNotEmpty(strRegExp) && objOptions.check_inactivity.value() == true;
			JSCmdBase.flgRaiseOKException = false;
			JSCmdBase.flgLogXML = false;

//			JSCmdShowJob objShowJob = objSchedulerObjectFactory.isJobRunning(strJobName);
			JSCmdShowJob objShowJob = objSchedulerObjectFactory.getTaskQueue(strJobName);
			if (objShowJob == null) {
				doStartJob(objSchedulerObjectFactory, strJobName, strJobChainName, strOrderName);
				sleep(intTimeBetweenCheck * 1000); // give JS time to start the job and to get stdout/stderr
			}
			else {
				logger.info(String.format("Job '%1$s' is already running.", strJobName));
			}

			for (int intNoOfRestarts = 0; intNoOfRestarts < intMaxNoOfRestarts;) {
				objShowJob = objSchedulerObjectFactory.isJobRunning(strJobName);
				if (objShowJob == null) { // job is not running ...
					flgIsJobRunning = false;
					JSCmdShowHistory objHist = objSchedulerObjectFactory.createShowHistory();
					objHist.setJob(strJobName);
					objHist.run();
					Answer objHistA = objHist.getAnswer();
//					logger.debug(objSchedulerObjectFactory.getLastAnswer());
					List<HistoryEntry> objEntries = objHistA.getHistory().getHistoryEntry();
					if (objEntries != null) {
						HistoryEntry objEntry = objEntries.get(0);
						if (objEntry != null) {
							int intLastExitCode = objEntry.getExitCode().intValue();
							if (intLastExitCode != 0) {
								logger.info(String.format("Exitcode of last Execution was '%1$d'", intLastExitCode));
							}
							else {
								// ExitCode is null
								doStartJob(objSchedulerObjectFactory, strJobName, strJobChainName, strOrderName);
								flgIsJobRunning = true;
								if (objOptions.mail_on_restart.value() == true) {
									sendMail(objOptions.getMailOnRestartOptions());
								}
							}
						}
					}
				}
				else { // job is already running ...
					flgIsJobRunning = true;
					Answer objAnswer = objShowJob.getAnswer();
					Job objJobAnswer = objAnswer.getJob();

					int intNoOfTasks = objJobAnswer.getTasks().getCount().intValue();
					logger.debug(String.format("Number of tasks currently running is '%1$d'", intNoOfTasks));
					if (intNoOfTasks > 0) {
						Task objTask = objJobAnswer.getTasks().getTask().get(0);
						int intTaskID = objTask.getTask().intValue();
						if (intLastTaskID == 0) {
							intLastTaskID = intTaskID;
						}
						if (objTask != null && flgCheckInactivity == true) { // get the heartbeats from the log ...
							Log objTaskLog = objTask.getLog();
							String strT = objTaskLog.getContent();

							// count Heartbeats
							if (isNotEmpty(strRegExp)) { // The JS is getting the stdout/stderr every 10 seconds.
								if (intTaskID == intLastTaskID) {
									int intHeartBeatCount = GrepCount(strT, strRegExp);
									if (intHeartBeatCount <= intLastHeartBeatCount) {
										// TODO: method KillTask with jobname, taskid
										JSCmdKillTask objKiller = objSchedulerObjectFactory.createKillTask();
										objKiller.setId(objTask.getId());
										objKiller.setImmediately("true");
										objKiller.setJob(strJobName);
										objKiller.run();
										logger.info(String.format("Job '%1$s' killed due to missing heartbeats '%2$s'. Count is '%3$d', LastCount was '%4$s'",
												strJobName, strRegExp, intHeartBeatCount, intLastHeartBeatCount));
										// TODO Mail schreiben
										if (objOptions.mail_on_nonactivity.value() == true) {
											sendMail(objOptions.getMailOnKillOptions());
										}
									}
									intLastHeartBeatCount = intHeartBeatCount;
								}
								else { // another task, count the heartbeats from scratch
									intLastHeartBeatCount = 0;
									intLastTaskID = intTaskID;
								}
							}
						}
					}
					else {
						flgIsJobRunning = false;
					}
				}

				if (flgIsJobRunning == false) {
					doStartJob(objSchedulerObjectFactory, strJobName, strJobChainName, strOrderName);
					intNoOfRestarts++;
					logger.info(String.format("Job started again. Number of restarts is '%1$d'", intNoOfRestarts));
					sleep(intTimeBetweenCheck * 1000);
					flgIsJobRunning = true;
					intLastHeartBeatCount = 0;
					intLastTaskID = 0;
					if (objOptions.mail_on_restart.value() == true) {
						sendMail(objOptions.getMailOnRestartOptions());
					}
				}
				else {
					logger.debug(String.format("sleep for '%2$d' seconds, number of restarts is '%1$d'", intNoOfRestarts, intTimeBetweenCheck));
					sleep(intTimeBetweenCheck * 1000);
				}
			} // end for

		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format(Messages.getMsg("JSJ-I-107"), conMethodName), e);
		}
		finally {
			logger.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName));
		}

		return this;
	}

	private void doStartJob(final SchedulerObjectFactory objSchedulerObjectFactory, final String strJobName, final String strJobChainName, final String strOrderName) {
		if (isNotEmpty(strOrderName)) {
			@SuppressWarnings("unused")
			JSCmdModifyOrder objOrder = objSchedulerObjectFactory.StartOrder(strJobChainName, strOrderName);
		}
		else {
			@SuppressWarnings("unused")
			JSCmdStartJob objSJob = objSchedulerObjectFactory.StartJob(strJobName);
		}
		logger.info(String.format("Job '%1$s' started.", strJobName));
	}

	private int GrepCount(final String pstrText, final String pstrRegExp) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::GrepCount";

		Pattern p = Pattern.compile(pstrRegExp);
		BufferedReader buff = new BufferedReader(new StringReader(pstrText));
		int intCount = 0;
		try {
			String a;
			for (a = buff.readLine(); a != null; a = buff.readLine()) {
				Matcher m = p.matcher(a);
				if (m.find()) {
					intCount++;
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return intCount;
	} // private int GrepCount

	private void sleep(final int pintMillis) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::sleep";

		try {
			Thread.sleep(pintMillis);
		}
		catch (InterruptedException e) {
		}

	} // private void sleep

	/**
	 * send mail 
	 * 
	 * @param recipient
	 * @param recipientCC carbon copy recipient
	 * @param recipientBCC blind carbon copy recipient
	 * @param subject
	 * @param body
	 * @throws Exception
	 */
	protected void sendMail(final ISOSSmtpMailOptions pobjO) throws Exception {

		final String strDelims = ",|;";
		try {
			SOSMail sosMail = new SOSMail(pobjO.gethost().Value());
			sosMail.setPort(pobjO.getport().Value());

			sosMail.setQueueDir(pobjO.getqueue_directory().Value());
			sosMail.setFrom(pobjO.getfrom().Value());
			sosMail.setContentType(pobjO.getcontent_type().Value());
			sosMail.setEncoding(pobjO.getencoding().Value()); 

			String recipient = pobjO.getto().Value();
			String recipients[] = recipient.trim().split(strDelims);
			for (String recipient2 : recipients) {
				sosMail.addRecipient(recipient2.trim());
			}

			String recipientCC = pobjO.getcc().Value();
			if (recipientCC.trim().length() > 0) {
			String recipientsCC[] = recipientCC.trim().split(strDelims);
			for (String element : recipientsCC) {
				sosMail.addCC(element.trim());
			}
			}

			String recipientBCC = pobjO.getbcc().Value().trim();
			if (recipientBCC.length() > 0) {
			String recipientsBCC[] = recipientBCC.trim().split(strDelims);
			for (String element : recipientsBCC) {
				sosMail.addBCC(element.trim());
			}
			}

			sosMail.setSubject(pobjO.getsubject().Value());
			sosMail.setBody(pobjO.getbody().Value());
			
	        SOSStandardLogger sosLogger = new SOSStandardLogger(SOSStandardLogger.DEBUG9);
			sosMail.setSOSLogger(sosLogger);

			logger.debug("sending mail: \n" + sosMail.dumpMessageAsString());

			if (!sosMail.send()) {
				logger.warn("mail server is unavailable, mail for recipient [" + recipient + "] is queued in local directory [" + sosMail.getQueueDir() + "]:"
						+ sosMail.getLastError());
			}

			sosMail.clearRecipients();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception("error occurred sending mail: " + e.getMessage());
		}
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override
	public String myReplaceAll(final String pstrSourceString, final String pstrReplaceWhat, final String pstrReplaceWith) {

		String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
		return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
	}

	/**
	 * 
	 * \brief replaceSchedulerVars
	 * 
	 * \details
	 * Dummy-Method to make sure, that there is always a valid Instance for the JSJobUtilities.
	 * \return 
	 *
	 * @param isWindows
	 * @param pstrString2Modify
	 * @return
	 */
	@Override
	public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
		logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
		return pstrString2Modify;
	}

	/**
	 * 
	 * \brief setJSParam
	 * 
	 * \details
	 * Dummy-Method to make shure, that there is always a valid Instance for the JSJobUtilities.
	 * \return 
	 *
	 * @param pstrKey
	 * @param pstrValue
	 */
	@Override
	public void setJSParam(final String pstrKey, final String pstrValue) {

	}

	@Override
	public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {

	}

	/**
	 * 
	 * \brief setJSJobUtilites
	 * 
	 * \details
	 * The JobUtilities are a set of methods used by the SSH-Job or can be used be other, similar, job-
	 * implementations.
	 * 
	 * \return void
	 *
	 * @param pobjJSJobUtilities
	 */
	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {

		if (pobjJSJobUtilities == null) {
			objJSJobUtilities = this;
		}
		else {
			objJSJobUtilities = pobjJSJobUtilities;
		}
		logger.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
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

	@Override public void setNextNodeState(final String pstrNodeName) {
		// TODO Auto-generated method stub
		
	}

} // class JobSchedulerLaunchAndObserve