package sos.scheduler.CheckRunHistory;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import sos.spooler.Mail;
import sos.spooler.Spooler;
import sos.util.SOSDate;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Answer;
import com.sos.scheduler.model.answers.ERROR;
import com.sos.scheduler.model.answers.HistoryEntry;
import com.sos.scheduler.model.commands.JSCmdShowHistory;
import com.sos.scheduler.model.commands.ShowHistory;
import com.sos.scheduler.model.exceptions.JSCommandErrorException;
 
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerCheckRunHistory extends JSToolBox implements JSJobUtilities, IJSCommands {
	private final String							conSVNVersion		= "$Id$";
	private final String							conClassName		= "JobSchedulerCheckRunHistory";						//$NON-NLS-1$
	private static Logger							logger				= Logger.getLogger(JobSchedulerCheckRunHistory.class);
	protected JobSchedulerCheckRunHistoryOptions	objOptions			= null;
	private JSJobUtilities							objJSJobUtilities	= this;
	private IJSCommands								objJSCommands		= this;

 
	public JobSchedulerCheckRunHistory() {
		super();
		Messages = new Messages("com_sos_scheduler_messages", Locale.getDefault());
	}

	 
	public JobSchedulerCheckRunHistoryOptions Options() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$
		if (objOptions == null) {
			objOptions = new JobSchedulerCheckRunHistoryOptions();
		}
		return objOptions;
	}
 
	public JobSchedulerCheckRunHistoryOptions Options(final JobSchedulerCheckRunHistoryOptions pobjOptions) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$
		objOptions = pobjOptions;
		return objOptions;
	}
 
	public JobSchedulerCheckRunHistory Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$
		logger.info(conSVNVersion);
		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
		try {
			Object objSp = objJSCommands.getSpoolerObject();
			boolean flgRunAsSchedulerAPIJob = objSp != null;
			
			@SuppressWarnings("unused")
			String strOperation = Options().operation.Value();
			String strJobName = Options().JobName.Value();
			String strMessage = Options().message.Value();
			String strMailTo  = Options().mail_to.Value();
			String strMailCc  = Options().mail_cc.Value();
			String strMailBcc = Options().mail_bcc.Value();
			
			strMessage = Messages.getMsg("JCH_T_0001", strJobName, myReplaceAll(strMessage,"\\[?JOB_NAME\\]?", strJobName));
			Date objDateStartTime = Options().start_time.getDateObject();
			
			if(flgRunAsSchedulerAPIJob) {
				Spooler objSpooler = (Spooler) objSp;
				Mail objMail = objSpooler.log().mail();
				if(isNotEmpty(strMailTo)) {
					objMail.set_to(strMailTo);
				}
				if(isNotEmpty(strMailCc)) {
					objMail.set_cc(strMailCc);
				}
				if(isNotEmpty(strMailBcc)) {
					objMail.set_bcc(strMailBcc);
				}
				if(isNotEmpty(strMessage)) {
					objMail.set_subject(strMessage);
				}
				if(Options().SchedulerHostName.isDirty() == false) {
					Options().SchedulerHostName.Value(objSpooler.hostname());
				}
				if(Options().scheduler_port.isDirty() == false) {
					Options().scheduler_port.value(objSpooler.tcp_port());
				}
			}
	 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			 
			SchedulerObjectFactory objJSFactory = new SchedulerObjectFactory();
			objJSFactory.initMarshaller(ShowHistory.class);
			JSCmdShowHistory objShowHistory = objJSFactory.createShowHistory();
			objShowHistory.setJob(strJobName);
			objShowHistory.setPrev(BigInteger.valueOf(30));
			Answer objAnswer = null;
			 
			if(objAnswer != null) {
				ERROR objError = objAnswer.getERROR();
				if(objError != null) {
					throw new JSCommandErrorException(objError.getText());
				}
				List<HistoryEntry> objHistoryEntries = objAnswer.getHistory().getHistoryEntry();
				HistoryEntry completedHistoryEntry=null;
				for (HistoryEntry historyItem : objHistoryEntries) {
						if ((historyItem.getEndTime() != null) ){
							completedHistoryEntry = historyItem;
						}
				}
				
				if ((objHistoryEntries.size() == 0) || (completedHistoryEntry == null)) {
					logger.error(Messages.getMsg("JCH_E_0001", strJobName));
					throw new JobSchedulerException(Messages.getMsg("JCH_E_0001", strJobName));
				}
				else {
					HistoryEntry objHistoryEntry = completedHistoryEntry;
					String strErrorText = objHistoryEntry.getErrorText();
					String strEndTime = objHistoryEntry.getEndTime();
					boolean flgRunTooLate = false;
					String objEndDateTimeStr = "";
					if(strEndTime.endsWith("Z")) {
						DateTimeFormatter dateTimeFormatter  =  DateTimeFormat.forPattern("yyyy-MM-dd'T'H:mm:ss.SSSZ");
						DateTime objEndDateTime = dateTimeFormatter.parseDateTime(strEndTime.replaceFirst("Z", "+00:00"));
						DateTime objStartDateTime = new DateTime(objDateStartTime);
						objEndDateTimeStr = objEndDateTime.toString();
						flgRunTooLate = objEndDateTime.toLocalDateTime().isBefore(objStartDateTime.toLocalDateTime());
					}
					else {
						Date objEndDateTime = SOSDate.getDate(strEndTime, SOSDate.dateTimeFormat);
						objEndDateTimeStr = objEndDateTime.toString();
						flgRunTooLate = objEndDateTime.before(objDateStartTime);
					}
					boolean flgRunSuccessful = isEmpty(strErrorText);
					
					if(flgRunTooLate || !flgRunSuccessful ) {
						logger.info(Messages.getMsg("JCH_I_0001", strJobName, objEndDateTimeStr));
						if(!flgRunSuccessful) {
							logger.info(Messages.getMsg("JCH_I_0002", strErrorText));
						}
						logger.error(strMessage);
						throw new JobSchedulerException(strMessage);
					}
					else {
						logger.info(Messages.getMsg("JCH_I_0003", strJobName, objEndDateTimeStr));
					}
				}
			}
			else {
				throw new JobSchedulerException(Messages.getMsg("JSJ_E_0140",Options().SchedulerHostName.Value(),Options().scheduler_port.value()));
			}
		}
		catch (Exception e) {
			logger.error(Messages.getMsg("JSJ-F-107", conMethodName), e);
			throw e;
		}
		return this;
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

	 
	@Override
	public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
		logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
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
		}
		else {
			objJSJobUtilities = pobjJSJobUtilities;
		}
		logger.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
	}

	 
	public void setJSCommands(final IJSCommands pobjJSCommands) {
		if (pobjJSCommands == null) {
			objJSCommands = this;
		}
		else {
			objJSCommands = pobjJSCommands;
		}
		logger.debug("pobjJSCommands = " + pobjJSCommands.getClass().getName());
	}

	@Override
	public String getCurrentNodeName() {
		return null;
	}

	@Override
	public Object getSpoolerObject() {
		return null;
	}

	@Override
	public String executeXML(final String pstrJSXmlCommand) {
		return null;
	}

	@Override
	public void setStateText(final String pstrStateText) {
	}

	@Override
	public void setCC(final int pintCC) {
	}

	@Override public void setNextNodeState(final String pstrNodeName) {
		// TODO Auto-generated method stub
		
	}


} // class JobSchedulerCheckRunHistory