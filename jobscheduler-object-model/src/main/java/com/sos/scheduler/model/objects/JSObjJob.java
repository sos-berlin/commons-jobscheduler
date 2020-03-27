package com.sos.scheduler.model.objects;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.LanguageDescriptorList;
import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjJob extends Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjJob.class);
    private Script objScript = null;
    public static final String[] ValidLanguages4Job = LanguageDescriptorList.getLanguages4APIJobs();
    public static final String[] ValidLanguages4Monitor = LanguageDescriptorList.getLanguages4Monitor();
    public static final String InternalAPIMethodNames = "spooler_process_before;spooler_process_after;spooler_task_before;spooler_task_after;";
    public static final String MonitorMethodNames = "spooler_task_before;spooler_task_after;spooler_process_before;spooler_process_after";
    public static final String fileNameExtension = ".job.xml";
    public static final String[] ValidLogLevels = new String[] { "info", "debug1", "debug2", "debug3", "debug4", "debug5", "debug6", "debug7",
            "debug8", "debug9", "" };
    JobSettings objSettings = null;

    public static enum enuVisibilityTypes {
        enuIsVisible, enuIsNotVisible, enuIsNeverVisible;

        private static final String[] VISIBILITY_TEXT = new String[] { "", "yes", "no", "never" };
        public static boolean isMandatory = false;
        public static String i18nKey = "job.visible";

        public static String[] getTexts() {
            return VISIBILITY_TEXT;
        }
    }

    public JSObjJob(final SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
        super.strFileNameExtension = fileNameExtension;
    }

    public JSObjJob(final SchedulerObjectFactory schedulerObjectFactory, final Job origOrder) {
        objFactory = schedulerObjectFactory;
        super.strFileNameExtension = fileNameExtension;
        setObjectFieldsFrom(origOrder);
        afterUnmarshal();
    }

    public JSObjJob(final SchedulerObjectFactory schedulerObjectFactory, final ISOSVirtualFile pobjVirtualFile) {
        super();
        super.strFileNameExtension = fileNameExtension;
        objFactory = schedulerObjectFactory;
        final Job objJob = (Job) unMarshal(pobjVirtualFile);
        setObjectFieldsFrom(objJob);
        setHotFolderSrc(pobjVirtualFile);
    }

    private void afterUnmarshal() {
        if (this.getDescription() != null) {
            removeEmptyContentsFrom(this.getDescription().getContent());
        }
        if (this.getScript() != null) {
            removeEmptyContentsFrom(this.getScript().getContent());
        }
        if (this.getMonitor() != null) {
            for (final Job.Monitor objMonitor : this.getMonitor()) {
                if (objMonitor.getScript() != null) {
                    removeEmptyContentsFrom(objMonitor.getScript().getContent());
                }
            }
        }
    }

    public boolean hasDescription() {
        return description != null;
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = objFactory.createJobDescription();
            setDirty();
        }
        return description;
    }

    @Override
    public Script getScript() {
        if (script == null) {
            objScript = objFactory.createScript();
            script = objScript;
            setDirty();
        } else {
            objScript = script;
        }
        return script;
    }

    private void removeEmptyContentsFrom(final List<Object> objList) {
        final List<String> emptyContents = new ArrayList<String>();
        for (final Object listItem : objList) {
            if (listItem instanceof String && ((String) listItem).trim().isEmpty()) {
                emptyContents.add((String) listItem);
            }
        }
        objList.removeAll(emptyContents);
    }

    public void setOrder(final boolean pflgIsOrder) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        if (pflgIsOrder) {
            this.setOrder("yes");
        } else {
            this.setOrder("no");
        }
    }

    public void setForceIdleTimeout(final boolean pflgIsForcedIdleTimeout) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        if (pflgIsForcedIdleTimeout) {
            this.setForceIdleTimeout("yes");
        } else {
            this.setForceIdleTimeout("no");
        }
    }

    public void setTemporary(final boolean pflgIsTemporary) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        if (pflgIsTemporary) {
            this.setTemporary("yes");
        } else {
            this.setTemporary("no");
        }
        setDirty();
    }

    public boolean isScriptEmpty() {
        boolean flgIsEmpty = true;
        if (script != null && !script.getContent().isEmpty()) {
            flgIsEmpty = false;
        }
        return flgIsEmpty;
    }

    public boolean hasScript() {
        objScript = getScript();
        if (objScript == null) {
            final JobSchedulerException objJSException = new JobSchedulerException("Job has no script.");
            LOGGER.error("", objJSException);
            throw objJSException;
        }
        return true;
    }

    public boolean getEnabled() {
        return true;
    }

    public boolean isJobChainJob() {
        return isOrderDrivenJob();
    }

    public boolean isOrderDrivenJob() {
        if (order == null) {
            return false;
        } else {
            return getYesOrNo(order);
        }
    }

    @Override
    public String getProcessClass() {
        String strT = super.processClass;
        if (strT == null || strT.isEmpty()) {
            strT = "";
        }
        return avoidNull(strT);
    }

    @Override
    public JSObjParams getParams() {
        return new JSObjParams(objFactory, super.getParams());
    }

    @Override
    public Process getProcess() {
        Process objP = super.getProcess();
        if (objP == null) {
            super.setProcess(new Process());
            objP = super.getProcess();
            setDirty();
        }
        return objP;
    }

    public void setTasks(final String pstrV) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        BigInteger intT = new BigInteger(pstrV);
        super.setTasks(intT);
    }

    public String getMintasks() {
        return bigInt2String(super.getMinTasks());
    }

    public String getTasksAsString() {
        return bigInt2String(super.getTasks());
    }

    public void setMintasks(final String mintasks) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        BigInteger bigI = new BigInteger(mintasks);
        super.setMinTasks(bigI);
    }

    @Override
    public RunTime getRunTime() {
        RunTime objP = super.getRunTime();
        if (objP == null) {
            super.setRunTime(new RunTime());
            objP = super.getRunTime();
            setDirty();
        }
        return objP;
    }

    public JSObjRunTime getRunTimeObj() {
        RunTime objP = super.getRunTime();
        if (objP == null) {
            super.setRunTime(new RunTime());
            objP = super.getRunTime();
            setDirty();
        }
        return new JSObjRunTime(objFactory, objP);
    }

    public String getJobName() {
        String name = this.getHotFolderSrc().getName();
        name = name.substring(0, name.indexOf(JSObjJob.fileNameExtension));
        name = new File(name).getName();
        return name;
    }

    public String getJobNameAndTitle() {
        String strT = this.getJobName();
        if (this.isDisabled()) {
            strT += " (disabled)";
        }
        String strV = this.getTitle();
        if (strV != null && !strV.isEmpty()) {
            strT += " - " + this.getTitle();
        }
        return strT;
    }

    public boolean isDisabled() {
        return false;
    }

    @Override
    public void setVisible(final String visible) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        super.setVisible(visible);
    }

    @Override
    public void setWarnIfLongerThan(final String warnIfLongerThan) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        super.setWarnIfLongerThan(warnIfLongerThan);
    }

    @Override
    public void setWarnIfShorterThan(final String pstrWarnIfShorterThan) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        super.setWarnIfShorterThan(pstrWarnIfShorterThan);
    }

    public int languageAsInt(final String language) {
        if (language != null) {
            String strT = language.toLowerCase();
            for (int i = 0; i < ValidLanguages4Job.length; i++) {
                if (ValidLanguages4Job[i].equalsIgnoreCase(strT)) {
                    return i;
                }
            }
        }
        return 0;
    }

    private String languageAsString(final int language) {
        String strR = "";
        if (language >= 0) {
            strR = ValidLanguages4Job[language];
        }
        return strR;
    }

    public String getLanguage(final int language) {
        return ValidLanguages4Job[language];
    }

    public int getLanguage() {
        return languageAsInt(this.getScript().getLanguage());
    }

    public String getLanguageAsString(final int language) {
        return languageAsString(language);
    }

    public void setLanguage(final String pstrLanguage) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        this.setLanguage(languageAsInt(pstrLanguage));
    }

    public void setLanguage(final int language) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        this.getScript().setLanguage(languageAsString(language));
    }

    public boolean isJava() {
        return "java".equalsIgnoreCase(languageAsString(getLanguage()));
    }

    public boolean isInternalAPIJob() {
        return !"shell".equalsIgnoreCase(languageAsString(getLanguage()));
    }

    @Override
    public void setName(final String name) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        super.setName(name);
    }

    public void setJavaClass(final String javaClass) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        this.getScript().setJavaClass(javaClass);
    }

    public void setClasspath(final String classpath) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        this.getScript().setJavaClassPath(classpath);
    }

    public void setHistoryOnProcess(final String pstrValue) {
        this.setSettings().setHistoryOnProcess(pstrValue);
        setDirty();
    }

    public void setHistory(final String pstrValue) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        this.setSettings().setHistory(pstrValue);
    }

    public void setHistoryWithLog(final String pstrValue) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        this.setSettings().setHistoryWithLog(pstrValue);
    }

    public void setOrdering(final String ordering) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        getMonitor().get(0).setOrdering(new BigInteger(ordering));
    }

    private String getYesNoText(final String pstrS) {
        String strR = pstrS;
        if (strR == null || strR.trim().isEmpty()) {
            strR = "no";
        }
        return strR;
    }

    public void setMailOnError(final String text) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        this.setSettings().setMailOnError(getYesNoText(text));
    }

    public void setMailOnWarning(final String text) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        this.setSettings().setMailOnWarning(getYesNoText(text));
    }

    public void setMailOnSuccess(final String text) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        this.setSettings().setMailOnSuccess(getYesNoText(text));
    }

    public void setMailOnProcess(final String text) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        this.setSettings().setMailOnProcess(getYesNoText(text));
    }

    public void setMailOnDelayAfterError(final String text) {
        if (!canUpdate()) {
            return;
        }
        this.setSettings().setMailOnDelayAfterError(getYesNoText(text));
    }

    public void setLogMailTo(final String text) {
        if (!canUpdate()) {
            return;
        }
        this.setSettings().setLogMailTo(getYesNoText(text));
    }

    public void setLogMailBcc(final String text) {
        if (!canUpdate()) {
            return;
        }
        this.setSettings().setLogMailBcc(getYesNoText(text));
    }

    public void setLogMailCc(final String text) {
        if (!canUpdate()) {
            return;
        }
        this.setSettings().setLogMailCc(getYesNoText(text));
    }

    public String getLogLevel() {
        JobSettings objS = this.getSettings();
        if (objS != null) {
            LogLevel objL = objS.getLogLevel();
            if (objL != null) {
                return objL.value();
            }
        }
        return "";
    }

    public void setLogLevel(final String text) {
        if (!canUpdate()) {
            return;
        }
        this.setSettings().setLogLevel(LogLevel.fromValue(text));
    }

    @Override
    public void setIdleTimeout(final String idleTimeout) {
        if (!canUpdate()) {
            return;
        }
        super.setIdleTimeout(idleTimeout);
    }

    public void setForceIdletimeout(final boolean forceIdleTimeout) {
        if (!canUpdate()) {
            return;
        }
        super.setTimeout(setYesOrNo(forceIdleTimeout));
    }

    public void setStopOnError(final boolean stopOnError) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        super.setStopOnError(setYesOrNo(stopOnError));
    }

    public void setReplace(final boolean replace) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        super.setReplace(setYesOrNo(replace));
    }

    public boolean isReplace() {
        return getYesOrNo(super.getReplace());
    }

    public void setTemporary1(final boolean temporary) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        super.setTemporary(setYesOrNo(temporary));
    }

    public boolean isTemporary() {
        return getYesOrNo(super.getTemporary());
    }

    public void setVisible1(final String visible) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        super.setVisible(visible);
    }

    @Override
    public String getVisible() {
        return avoidNull(super.getVisible());
    }

    public void setFile(final String file) {
        if (!canUpdate()) {
            return;
        }
        setDirty();
        this.getProcess().setFile(file);
    }

    public String getHistoryWithLog() {
        if (this.getSettings() != null) {
            return avoidNull(this.getSettings().getHistoryWithLog());
        }
        return "";
    }

    public String getHistory() {
        if (this.getSettings() != null) {
            return avoidNull(avoidNull(this.getSettings().getHistory()));
        }
        return "";
    }

    public String getHistoryOnProcess() {
        if (this.getSettings() != null) {
            return avoidNull(this.getSettings().getHistoryOnProcess());
        }
        return "";
    }

    public String getMonitorName() {
        return "";
    }

    public void setMonitorName(final String name) {
        //
    }

    public void setMailOnError(final String pstrValue, final String pstrDefaultValue) {
        this.getSettings().setMailOnError(oneOfUs(pstrValue, pstrDefaultValue));
        setDirty();
    }

    @Override
    public JobSettings getSettings() {
        JobSettings objS = super.getSettings();
        if (objS == null) {
            throw new JobSchedulerException("is null");
        }
        return objS;
    }

    public JobSettings setSettings() {
        if (settings == null) {
            settings = new JobSettings();
        }
        setDirty();
        return settings;
    }

    public String getMailOnError() {
        try {
            return avoidNull(getSettings().getMailOnError());
        } catch (Exception e) {
            //
        }
        return "";
    }

    public void setMailOnWarning(final String pstrValue, final String pstrDefaultValue) {
        this.setSettings().setMailOnWarning(oneOfUs(pstrValue, pstrDefaultValue));
        setDirty();
    }

    public String getMailOnWarning() {
        try {
            return avoidNull(getSettings().getMailOnWarning());
        } catch (Exception e) {
            //
        }
        return "";
    }

    public void setMailOnProcess(final String pstrValue, final String pstrDefaultValue) {
        this.setSettings().setMailOnProcess(oneOfUs(pstrValue, pstrDefaultValue));
        setDirty();
    }

    public String getMailOnProcess() {
        try {
            return avoidNull(getSettings().getMailOnProcess());
        } catch (Exception e) {
            //
        }
        return "";
    }

    public void setMailOnSuccess(final String pstrValue, final String pstrDefaultValue) {
        this.setSettings().setMailOnSuccess(oneOfUs(pstrValue, pstrDefaultValue));
        setDirty();
    }

    public String getMailOnSuccess() {
        try {
            return avoidNull(getSettings().getMailOnSuccess());
        } catch (Exception e) {
            //
        }
        return "";
    }

    public void setMailOnDelayAfterError(final String pstrValue, final String pstrDefaultValue) {
        this.setSettings().setMailOnDelayAfterError(oneOfUs(pstrValue, pstrDefaultValue));
        setDirty();
    }

    public String getMailOnDelayAfterError() {
        try {
            return avoidNull(getSettings().getMailOnDelayAfterError());
        } catch (Exception e) {
            //
        }
        return "";
    }

    public void setLogMailTo(final String pstrValue, final String pstrDefaultValue) {
        this.setSettings().setLogMailTo(oneOfUs(pstrValue, pstrDefaultValue));
        setDirty();
    }

    public String getLogMailTo() {
        try {
            return avoidNull(getSettings().getLogMailTo());
        } catch (Exception e) {
            //
        }
        return "";
    }

    public void setLogMailCC(final String pstrValue, final String pstrDefaultValue) {
        this.setSettings().setLogMailCc(oneOfUs(pstrValue, pstrDefaultValue));
        setDirty();
    }

    public String getLogMailCC() {
        try {
            return avoidNull(getSettings().getLogMailCc());
        } catch (Exception e) {
            //
        }
        return "";
    }

    public void setLogMailBcc(final String pstrValue, final String pstrDefaultValue) {
        this.setSettings().setLogMailBcc(oneOfUs(pstrValue, pstrDefaultValue));
        setDirty();
    }

    public String getLogMailBCC() {
        try {
            return avoidNull(getSettings().getLogMailBcc());
        } catch (Exception e) {
            //
        }
        return "";
    }

    public String oneOfUs(final String pstrValue, final String pstrDefaultValue) {
        if (isNotEmpty(pstrValue)) {
            return pstrValue;
        }
        return pstrDefaultValue;
    }

    public boolean isSetbackDelay() {
        return !this.getDelayOrderAfterSetback().isEmpty();
    }

    public boolean isNotEmpty(final String pstrValue) {
        return pstrValue != null && !pstrValue.trim().isEmpty();
    }

}