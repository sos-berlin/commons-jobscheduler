package com.sos.JSHelper.Logging;

import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.io.Files.JSTextFile;

import org.apache.log4j.*;
import org.apache.log4j.net.SMTPAppender;

import java.util.Enumeration;
import java.util.Vector;

@Deprecated
public class Log4JHelper implements JSListener {

    private static final Logger LOGGER = Logger.getLogger(Log4JHelper.class);
    private static final String CLASSNAME = "Log4JHelper";
    public static final String PROPFILE = "log4j.properties";
    public static final String MAIL_APPENDER = "mail";
    public static final String FILE_APPENDER = "mailfile";
    public static boolean flgUseJobSchedulerLog4JAppender = false;
    private String strFileAppender = FILE_APPENDER;
    private final Vector<String> objFiles = new Vector<String>();
    public String Subject = CLASSNAME;
    private String strName = CLASSNAME;
    private Level objLevel;
    private boolean flgPrintComputerName = false;
    private static String strPropfileName = null;
    private static Logger objCurrentLog = null;
    private static Logger objRootLog = null;

    public Log4JHelper() {
        configure(PROPFILE, CLASSNAME);
    }

    public Log4JHelper(final String pstrPropFile) {
        configure(pstrPropFile, CLASSNAME);
    }

    public Log4JHelper(final String pstrPropFile, final String pstrName) {
        configure(pstrPropFile, pstrName);
    }

    private void configure(final String pstrPropFileName, final String pstrName) {
        if (pstrPropFileName == null || "./log4j.properties".equalsIgnoreCase(pstrPropFileName)) {
            JSOptionsClass objO = new JSOptionsClass();
            String strF = objO.log4jPropertyFileName.Value();
            if (strF != null) {
                strPropfileName = strF;
            }
        } else {
            strPropfileName = pstrPropFileName;
            if ("null".equalsIgnoreCase(strPropfileName)) {
                if (!Logger.getRootLogger().getAllAppenders().hasMoreElements()) {
                    BasicConfigurator.configure();
                }
                return;
            }
        }
        strName = pstrName;
        JSTextFile objFile = new JSTextFile(strPropfileName);
        boolean flgNew = false;
        boolean flgPropFileIsOk = false;
        if (objFile != null && objFile.getParentFile() != null) {
            if (!objFile.exists() && objFile.getParentFile().canWrite()) {
                try {
                    objFile.WriteLine("log4j.rootCategory=info, stdout");
                    if (!flgUseJobSchedulerLog4JAppender) {
                        objFile.WriteLine("log4j.appender.stdout=org.apache.log4j.ConsoleAppender");
                    } else {
                        objFile.WriteLine("log4j.appender.stdout=com.sos.scheduler.JobSchedulerLog4JAppender");
                    }
                    objFile.WriteLine("log4j.appender.stdout.layout=org.apache.log4j.PatternLayout");
                    objFile.WriteLine("log4j.appender.stdout.layout.ConversionPattern=%5p [%t] (%F:%L) - %m%n");
                    objFile.close();
                    flgNew = true;
                    flgPropFileIsOk = true;
                } catch (Exception e) {
                    LOGGER.error(CLASSNAME + ": unable to create the log4j-property-file " + objFile.getAbsolutePath());
                    LOGGER.error(e.getMessage(), e);
                    flgPropFileIsOk = false;
                }
            } else {
                flgPropFileIsOk = true;
            }
        }
        objRootLog = Logger.getRootLogger();
        if (flgPropFileIsOk) {
            PropertyConfigurator.configure(objFile.getAbsolutePath());
        } else {
            try {
                PatternLayout layout = new PatternLayout();
                layout.setConversionPattern("%5p [%t] (%p-%F::%M:%L) - %m%n");
                Appender consoleAppender = new ConsoleAppender(layout);
                objRootLog.addAppender(consoleAppender);
                objRootLog.setLevel(Level.INFO);
                objRootLog.debug("Log4J configured programmatically");
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        if (CLASSNAME.equals(pstrName)) {
            objCurrentLog = objRootLog;
        } else {
            objCurrentLog = Logger.getLogger(strName);
        }
        if (flgNew) {
            objRootLog.warn("log4j-property-file '" + objFile.getAbsolutePath() + "' does not exist - a simple default-file was created");
            objRootLog.debug("using log4j-property-file " + objFile.getAbsolutePath());
            objRootLog.warn("all log-entries will be written to the console");
        }
        objLevel = objCurrentLog.getLevel();
    }

    @Override
    public void message(final String pstrMsg) {
        objCurrentLog.warn(pstrMsg);
    }

    public void setLevel(final Level intLevel) {
        objCurrentLog.setLevel(intLevel);
    }

    public void restoreLevel() {
        setLevel(objLevel);
    }

    public void attachFile(final String pstrFilename) throws Exception {
        JSTextFile objF = new JSTextFile(pstrFilename);
        objF.MustExist();
        objFiles.add(pstrFilename);
    }

    public void setFileAppenderForMail(final String pstrFileAppender) {
        strFileAppender = pstrFileAppender;
    }

    public void logStackTrace(final Throwable e) {
        StackTraceElement objEle[] = e.getStackTrace();
        setLevel(Level.DEBUG);
        for (StackTraceElement s : objEle) {
            String strTarget = s.getFileName() + ":" + s.getLineNumber();
            if (s.getLineNumber() == -1)
                strTarget = "Unknown Source";
            if (s.getLineNumber() == -2)
                strTarget = "Native Method";
            objRootLog.debug("     at " + s.getClassName() + "." + s.getMethodName() + "(" + strTarget + ")");
        }
        restoreLevel();
    }

    public static String getPropertyFile() {
        return strPropfileName;
    }

    public FileAppender checkFileAppender(final String pstrAppender) throws MissingAppenderException, IllegalAppenderTypeException {
        Appender objApp = checkAppender(pstrAppender);
        if (!(objApp instanceof FileAppender)) {
            throw new IllegalAppenderTypeException("Kein Mailversand, da der Appender '" + pstrAppender + "' in '" + getPropertyFile()
                    + " nicht vom Typ 'FileAppender' ist");
        }
        return (FileAppender) objApp;
    }

    public SMTPAppender checkMailAppender(final String pstrAppender) throws MissingAppenderException, IllegalAppenderTypeException {
        Appender objApp = checkAppender(pstrAppender);
        if (!(objApp instanceof SMTPAppender)) {
            throw new IllegalAppenderTypeException("Kein Mailversand, da der Appender '" + pstrAppender + "' in '" + getPropertyFile()
                    + " nicht vom Typ 'SMTPAppender' ist");
        }
        return (SMTPAppender) objApp;
    }

    public Appender checkAppender(final String pstrAppender) throws MissingAppenderException {
        Appender objApp = null;
        objApp = objRootLog.getAppender(pstrAppender);
        if (objApp == null) {
            throw new MissingAppenderException("Kein Mailversand, da keine Mailkonfiguration in den log4j-properties definiert ist (Appender '" + pstrAppender
                    + "' ist nicht in '" + getPropertyFile() + "' vorhanden.");
        }
        return objApp;
    }

    public static void debugAppenders(final Logger pobjLogger) {
        objRootLog.debug("Alle Appenders des Loggers " + pobjLogger.getName());
        Enumeration objE = pobjLogger.getAllAppenders();
        while (objE.hasMoreElements()) {
            Appender objA = (Appender) objE.nextElement();
            objRootLog.debug(objA.getName());
        }
    }

    public boolean isSubjectPraefix() {
        return flgPrintComputerName;
    }

    public void setSubjectPraefix(final boolean pflgPrintComputerName) {
        flgPrintComputerName = pflgPrintComputerName;
    }

}
