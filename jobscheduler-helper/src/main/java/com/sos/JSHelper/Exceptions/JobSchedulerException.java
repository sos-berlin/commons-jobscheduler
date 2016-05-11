package com.sos.JSHelper.Exceptions;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.SOSMsg;

@I18NResourceBundle(baseName = "com.sos.JSHelper.messages", defaultLocale = "en")
public class JobSchedulerException extends RuntimeException {

    protected String strMessage = null;
    protected SOSMsg objSOSMsg = null;
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerException.class);
    private int intStatus = SUCCESS;
    private int intErrorNumber = 0;
    private Exception nestedException;
    private String strCategory = "???";
    private String strTyp = "???";
    private JSOptionsClass objOptions = null;
    private String strAdditionalText = null;
    private String streMailSubject = null;
    public static final int NONE = -2;
    public static final int UNDEFINED = -1;
    public static final int SUCCESS = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;
    public static final int PENDING = 3;
    public static final int SKIPPROCESSING = 4;
    public static final int FATAL = 5;
    public static final String CategoryIDocProcessing = "IDocs";
    public static final String CategoryDatabase = "Database";
    public static final String CategoryFileTransfer = "FileTransfer";
    public static final String CategoryFileHandling = "FileHandling";
    public static final String CategoryJobStart = "JobStart";
    public static final String CategoryOptions = "Options";
    public static final String CategoryIDocs = "IDocs";
    public static final String TypeSQL = "SQL";
    public static final String TypeTL = "TrafficLight";
    public static final String TypeSplitIDocs = "Splitter";
    public static final String TypeOptionMissing = "OptionValueMissing";
    public static final String TypeExport = "Export";
    public static final String TypeImport = "Import";
    public static final int conErrorNumberJobStopped = 123;
    public static final int conErrorNumberFtpError = 77;
    public static boolean gflgStackTracePrinted = false;
    public static String LastErrorMessage = "";
    public boolean flgStackTracePrinted = false;

    public JobSchedulerException() {
        super("*** JobSchedulerException ***");
    }

    public void ErrorNumber(final int pintErrorNumber) {
        intErrorNumber = pintErrorNumber;
    }

    public int ErrorNumber() {
        return intErrorNumber;
    }

    public JobSchedulerException(final String pstrMessage) {
        super(pstrMessage);
        setMessage(pstrMessage);
        this.Status(JobSchedulerException.ERROR);
    }

    public JobSchedulerException(final SOSMsg pobjMsg) {
        super(pobjMsg.get());
        objSOSMsg = pobjMsg;
        setMessage(pobjMsg.get());
        this.Status(JobSchedulerException.ERROR);
    }

    public JobSchedulerException(final String pstrMessage, final Exception e) {
        super(pstrMessage);
        setMessage(pstrMessage + " (" + e.getMessage() + ")");
        saveException(e);
    }

    private void saveException(final Exception e) {
        nestedException = e;
        if (e instanceof JobSchedulerException) {
            JobSchedulerException objXE = (JobSchedulerException) e;
            if (!objXE.flgStackTracePrinted) {
                LOGGER.trace("", e);
                objXE.flgStackTracePrinted = true;
            }
        } else {
            if (!gflgStackTracePrinted) {
                LOGGER.trace("", e);
                gflgStackTracePrinted = true;
            }
        }
    }

    public JobSchedulerException(final SOSMsg pobjMsg, final Exception e) {
        super(pobjMsg.get());
        strMessage = pobjMsg.get();
        objSOSMsg = pobjMsg;
        setMessage(strMessage + " (" + e.getMessage() + ")");
        saveException(e);
    }

    public JobSchedulerException(final Exception e) {
        super(e.getMessage());
        setMessage(e.getMessage());
        saveException(e);
    }

    public void setMessage(final String pstrMsg) {
        strMessage = pstrMsg;
        LastErrorMessage += pstrMsg + "\n";
    }

    public Exception getNestedException() {
        return nestedException;
    }

    public JobSchedulerException setNestedException(final Exception e) {
        nestedException = e;
        return this;
    }

    public int Status() {
        if (intStatus == UNDEFINED) {
            intStatus = PENDING;
        }
        return intStatus;
    }

    public void setIntStatus(int status) {
        intStatus = status;
    }

    public String StatusAction() {
        String strT;
        switch (intStatus) {
        case JobSchedulerException.ERROR:
            strT = "The Workflow/Task ended with Error-Status. ";
            break;
        case JobSchedulerException.PENDING:
            strT = "The Workflow/Task ended in Error.\n" + "The Workflow may not be completed.\n" + "further scheduling of this workflow is on hold";
            break;
        case JobSchedulerException.WARNING:
            strT = "The Workflow/Task ended with Error-Status. ";
            break;
        case JobSchedulerException.SUCCESS:
            strT = "SUCCESS";
            break;
        case JobSchedulerException.SKIPPROCESSING:
            strT = "SKIPPROCESSING";
            break;
        case JobSchedulerException.UNDEFINED:
            strT = "UNDEFINED";
            break;
        case JobSchedulerException.NONE:
            strT = "";
            break;
        default:
            strT = "????";
            break;
        }
        return strT;
    }

    public String StatusText() {
        String strT;
        switch (intStatus) {
        case JobSchedulerException.ERROR:
            strT = "ERROR";
            break;
        case JobSchedulerException.PENDING:
            strT = "PENDING";
            break;
        case JobSchedulerException.WARNING:
            strT = "WARNING";
            break;
        case JobSchedulerException.SUCCESS:
            strT = "SUCCESS";
            break;
        case JobSchedulerException.SKIPPROCESSING:
            strT = "SKIPPROCESSING";
            break;
        case JobSchedulerException.UNDEFINED:
            strT = "UNDEFINED";
            break;
        default:
            strT = "????";
            break;
        }
        return strT;
    }

    public JobSchedulerException Status(final int pintStatus) {
        intStatus = pintStatus;
        return this;
    }

    public String Category() {
        return strCategory;
    }

    public JobSchedulerException Category(final String pstrCategory) {
        strCategory = pstrCategory;
        return this;
    }

    public String Typ() {
        return strTyp;
    }

    public JobSchedulerException Typ(final String pstrTyp) {
        strTyp = pstrTyp;
        return this;
    }

    public String Message() {
        return strMessage;
    }

    public JobSchedulerException Message(final String pstrMessage) {
        strMessage = pstrMessage;
        return this;
    }

    public String ExceptionText() {
        String strT = "";
        strT += getText("Message ", strMessage);
        strT += "\n\n";
        strT += getText("Category", strCategory);
        strT += getText("Type    ", strTyp);
        if (objOptions != null) {
            strT += "\nOptions active:";
            strT += objOptions.toString();
        }
        if (strAdditionalText != null) {
            strT += "\nAdditional Text:\n" + strAdditionalText + "\n";
        }
        if (intStatus != NONE) {
            strT += "\nfurther Actions:\n\n";
            strT += StatusAction() + "\n";
        }
        if (nestedException != null) {
            strT += "\n" + nestedException.getMessage();
            strT += "\n" + nestedException.toString();
            strT += "\nStackTrace of nested Exception:\n" + StackTrace2String(nestedException) + "\n";
        } else {
            strT += "\nStackTrace :\n" + StackTrace2String(this) + "\n";
        }
        return strT;
    }

    protected String getText(final String conWhat, final String pstrS) {
        if (pstrS != null && !pstrS.isEmpty()) {
            return conWhat + ": " + pstrS + "\n";
        } else {
            return "";
        }
    }

    public String StackTrace2String(final Exception e) {
        String strT = "";
        final StackTraceElement arrStack[] = e.getStackTrace();
        for (final StackTraceElement objS : arrStack) {
            strT += objS.toString() + "\n";
        }
        return strT;
    }

    public JobSchedulerException Options(final JSOptionsClass pobjOptions) {
        objOptions = pobjOptions;
        return this;
    }

    public String AdditionalText() {
        return strAdditionalText;
    }

    public JobSchedulerException AdditionalText(final String pstrAdditionalText) {
        strAdditionalText = pstrAdditionalText;
        return this;
    }

    public String eMailSubject() {
        if (streMailSubject == null) {
            streMailSubject = "JS: " + StatusText() + " - " + strMessage;
        } else {
            streMailSubject = "JS: " + StatusText() + " - " + streMailSubject;
        }
        return streMailSubject;
    }

    public JobSchedulerException eMailSubject(final String pstreMailSubject) {
        streMailSubject = pstreMailSubject;
        return this;
    }

}