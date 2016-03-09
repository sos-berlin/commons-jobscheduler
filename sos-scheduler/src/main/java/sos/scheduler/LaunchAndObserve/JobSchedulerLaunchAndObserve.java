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
import com.sos.scheduler.model.commands.JSCmdShowHistory;
import com.sos.scheduler.model.commands.JSCmdShowJob;
import com.sos.scheduler.model.objects.Spooler;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerLaunchAndObserve extends JSToolBox implements JSJobUtilities {

    protected JobSchedulerLaunchAndObserveOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerLaunchAndObserve.class);
    private JSJobUtilities objJSJobUtilities = this;

    public JobSchedulerLaunchAndObserve() {
        super("com_sos_scheduler_messages");
    }

    public JobSchedulerLaunchAndObserveOptions Options() {
        if (objOptions == null) {
            objOptions = new JobSchedulerLaunchAndObserveOptions();
        }
        return objOptions;
    }

    public JobSchedulerLaunchAndObserveOptions Options(final JobSchedulerLaunchAndObserveOptions pobjOptions) {
        objOptions = pobjOptions;
        return objOptions;
    }

    public JobSchedulerLaunchAndObserve Execute() throws Exception {
        final String conMethodName = "JobSchedulerLaunchAndObserve::Execute";
        int intLastHeartBeatCount = 0;
        int intLastTaskID = 0;
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        Options().CheckMandatory();
        LOGGER.debug(Options().toString());
        SchedulerObjectFactory objSchedulerObjectFactory = new SchedulerObjectFactory(objOptions.scheduler_host.Value(), objOptions.scheduler_port.value());
        objSchedulerObjectFactory.initMarshaller(Spooler.class);
        try {
            boolean flgIsJobRunning = false;
            String strJobName = objOptions.job_name.Value();
            String strJobChainName = objOptions.order_jobchain_name.Value();
            String strOrderName = objOptions.OrderId.Value();
            String strRegExp = objOptions.check_for_regexp.Value();
            int intTimeBetweenCheck = objOptions.check_interval.value();
            int intMaxNoOfRestarts = 9999;
            boolean flgCheckInactivity = isNotEmpty(strRegExp) && objOptions.check_inactivity.value() == true;
            JSCmdBase.flgRaiseOKException = false;
            JSCmdBase.flgLogXML = false;
            JSCmdShowJob objShowJob = objSchedulerObjectFactory.getTaskQueue(strJobName);
            if (objShowJob == null) {
                doStartJob(objSchedulerObjectFactory, strJobName, strJobChainName, strOrderName);
                sleep(intTimeBetweenCheck * 1000);
            } else {
                LOGGER.info(String.format("Job '%1$s' is already running.", strJobName));
            }
            for (int intNoOfRestarts = 0; intNoOfRestarts < intMaxNoOfRestarts;) {
                objShowJob = objSchedulerObjectFactory.isJobRunning(strJobName);
                if (objShowJob == null) {
                    flgIsJobRunning = false;
                    JSCmdShowHistory objHist = objSchedulerObjectFactory.createShowHistory();
                    objHist.setJob(strJobName);
                    objHist.run();
                    Answer objHistA = objHist.getAnswer();
                    List<HistoryEntry> objEntries = objHistA.getHistory().getHistoryEntry();
                    if (objEntries != null) {
                        HistoryEntry objEntry = objEntries.get(0);
                        if (objEntry != null) {
                            int intLastExitCode = objEntry.getExitCode().intValue();
                            if (intLastExitCode != 0) {
                                LOGGER.info(String.format("Exitcode of last Execution was '%1$d'", intLastExitCode));
                            } else {
                                doStartJob(objSchedulerObjectFactory, strJobName, strJobChainName, strOrderName);
                                flgIsJobRunning = true;
                                if (objOptions.mail_on_restart.value()) {
                                    sendMail(objOptions.getMailOnRestartOptions());
                                }
                            }
                        }
                    }
                } else {
                    flgIsJobRunning = true;
                    Answer objAnswer = objShowJob.getAnswer();
                    Job objJobAnswer = objAnswer.getJob();
                    int intNoOfTasks = objJobAnswer.getTasks().getCount().intValue();
                    LOGGER.debug(String.format("Number of tasks currently running is '%1$d'", intNoOfTasks));
                    if (intNoOfTasks > 0) {
                        Task objTask = objJobAnswer.getTasks().getTask().get(0);
                        int intTaskID = objTask.getTask().intValue();
                        if (intLastTaskID == 0) {
                            intLastTaskID = intTaskID;
                        }
                        if (objTask != null && flgCheckInactivity) {
                            Log objTaskLog = objTask.getLog();
                            String strT = objTaskLog.getContent();
                            if (isNotEmpty(strRegExp)) {
                                if (intTaskID == intLastTaskID) {
                                    int intHeartBeatCount = GrepCount(strT, strRegExp);
                                    if (intHeartBeatCount <= intLastHeartBeatCount) {
                                        JSCmdKillTask objKiller = objSchedulerObjectFactory.createKillTask();
                                        objKiller.setId(objTask.getId());
                                        objKiller.setImmediately("true");
                                        objKiller.setJob(strJobName);
                                        objKiller.run();
                                        LOGGER.info(String.format("Job '%1$s' killed due to missing heartbeats '%2$s'. Count is '%3$d', "
                                                + "LastCount was '%4$s'", strJobName, strRegExp, intHeartBeatCount, intLastHeartBeatCount));
                                        if (objOptions.mail_on_nonactivity.value()) {
                                            sendMail(objOptions.getMailOnKillOptions());
                                        }
                                    }
                                    intLastHeartBeatCount = intHeartBeatCount;
                                } else {
                                    intLastHeartBeatCount = 0;
                                    intLastTaskID = intTaskID;
                                }
                            }
                        }
                    } else {
                        flgIsJobRunning = false;
                    }
                }
                if (!flgIsJobRunning) {
                    doStartJob(objSchedulerObjectFactory, strJobName, strJobChainName, strOrderName);
                    intNoOfRestarts++;
                    LOGGER.info(String.format("Job started again. Number of restarts is '%1$d'", intNoOfRestarts));
                    sleep(intTimeBetweenCheck * 1000);
                    flgIsJobRunning = true;
                    intLastHeartBeatCount = 0;
                    intLastTaskID = 0;
                    if (objOptions.mail_on_restart.value()) {
                        sendMail(objOptions.getMailOnRestartOptions());
                    }
                } else {
                    LOGGER.debug(String.format("sleep for '%2$d' seconds, number of restarts is '%1$d'", intNoOfRestarts, intTimeBetweenCheck));
                    sleep(intTimeBetweenCheck * 1000);
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.getMsg("JSJ-I-107"), conMethodName), e);
        } finally {
            LOGGER.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName));
        }
        return this;
    }

    private void doStartJob(final SchedulerObjectFactory objSchedulerObjectFactory, final String strJobName, final String strJobChainName,
            final String strOrderName) {
        if (isNotEmpty(strOrderName)) {
            objSchedulerObjectFactory.StartOrder(strJobChainName, strOrderName);
        } else {
            objSchedulerObjectFactory.StartJob(strJobName);
        }
        LOGGER.info(String.format("Job '%1$s' started.", strJobName));
    }

    private int GrepCount(final String pstrText, final String pstrRegExp) {
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
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return intCount;
    }

    private void sleep(final int pintMillis) {
        try {
            Thread.sleep(pintMillis);
        } catch (InterruptedException e) {
        }
    }

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
            if (!recipientCC.trim().isEmpty()) {
                String recipientsCC[] = recipientCC.trim().split(strDelims);
                for (String element : recipientsCC) {
                    sosMail.addCC(element.trim());
                }
            }
            String recipientBCC = pobjO.getbcc().Value().trim();
            if (!recipientBCC.isEmpty()) {
                String recipientsBCC[] = recipientBCC.trim().split(strDelims);
                for (String element : recipientsBCC) {
                    sosMail.addBCC(element.trim());
                }
            }
            sosMail.setSubject(pobjO.getsubject().Value());
            sosMail.setBody(pobjO.getbody().Value());
            SOSStandardLogger sosLogger = new SOSStandardLogger(SOSStandardLogger.DEBUG9);
            sosMail.setSOSLogger(sosLogger);
            LOGGER.debug("sending mail: \n" + sosMail.dumpMessageAsString());
            if (!sosMail.send()) {
                LOGGER.warn("mail server is unavailable, mail for recipient [" + recipient + "] is queued in local directory [" + sosMail.getQueueDir() + "]:"
                        + sosMail.getLastError());
            }
            sosMail.clearRecipients();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception("error occurred sending mail: " + e.getMessage());
        }
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    }

    @Override
    public String myReplaceAll(final String pstrSourceString, final String pstrReplaceWhat, final String pstrReplaceWith) {
        String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
        return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
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