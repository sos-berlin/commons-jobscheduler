package com.sos.JSHelper.Options;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import com.sos.JSHelper.interfaces.IJobSchedulerLoggingAppender;
import com.sos.JSHelper.io.Files.JSTextFile;

/** @author KB */
public class SOSOptionLog4JPropertyFile extends SOSOptionInFileName {

    private static final long serialVersionUID = -5291704259398563937L;
    public static final String conLOG4J_PROPERTIESDefaultFileName = "log4j.properties";
    private final String conClassName = this.getClass().getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SOSOptionLog4JPropertyFile.class);
    private static Logger objCurrentLog = null;
    private static Logger objRootLog = null;
    private static String strPropfileName = null;
    private String strParentClassName = conClassName;
    private Level objLevel = null;
    public static boolean flgUseJobSchedulerLog4JAppender = false;
    private IJobSchedulerLoggingAppender objLoggingAppender = null;

    public void setLoggingAppender(IJobSchedulerLoggingAppender pobjLoggingAppender) {
        objLoggingAppender = pobjLoggingAppender;
    }

    public SOSOptionLog4JPropertyFile(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue,
            String pPstrDefaultValue, boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    public Logger getLoggerInstance(final String pstrParentClassName) {
        if (this.getValue() == null || ("./" + conLOG4J_PROPERTIESDefaultFileName).equalsIgnoreCase(this.getValue())) {
            JSOptionsClass objO = new JSOptionsClass();
            String strF = objO.log4jPropertyFileName.getValue();
            if (strF != null) {
                strPropfileName = strF;
            }
        } else {
            strPropfileName = this.getValue();
            if ("null".equalsIgnoreCase(strPropfileName)) {
                BasicConfigurator.configure();
                return Logger.getRootLogger();
            }
        }
        strParentClassName = pstrParentClassName;
        JSTextFile objLog4JPropertyFile = new JSTextFile(strPropfileName);
        boolean flgNew = false;
        boolean flgPropFileIsOk = false;
        if (!objLog4JPropertyFile.exists() && objLog4JPropertyFile.getParentFile().canWrite()) {
            try {
                objLog4JPropertyFile.writeLine("log4j.rootCategory=info, stdout");
                if (flgUseJobSchedulerLog4JAppender == false) {
                    objLog4JPropertyFile.writeLine("log4j.appender.stdout=org.apache.log4j.ConsoleAppender");
                } else {
                    objLog4JPropertyFile.writeLine("log4j.appender.stdout=com.sos.scheduler.JobSchedulerLog4JAppender");
                }
                objLog4JPropertyFile.writeLine("log4j.appender.stdout.layout=org.apache.log4j.PatternLayout");
                objLog4JPropertyFile.writeLine("log4j.appender.stdout.layout.ConversionPattern=%5p [%t] (%F:%L) - %m%n");
                objLog4JPropertyFile.close();
                flgNew = true;
                flgPropFileIsOk = true;
            } catch (Exception e) {
                LOGGER.error(conClassName + ": unable to create the log4j-property-file " + objLog4JPropertyFile.getAbsolutePath());
                LOGGER.error(e.getMessage(), e);
                flgPropFileIsOk = false;
            }
        } else {
            flgPropFileIsOk = true;
        }
        objRootLog = Logger.getRootLogger();
        if (flgPropFileIsOk) {
            PropertyConfigurator.configure(objLog4JPropertyFile.getAbsolutePath());
        } else {
            try {
                PatternLayout layout = new PatternLayout();
                layout.setConversionPattern("%5p [%t] (%p-%F::%M:%L) - %m%n");
                Appender consoleAppender = null;
                if (flgUseJobSchedulerLog4JAppender == true && objLoggingAppender != null) {
                    if (objLoggingAppender instanceof Appender) {
                        consoleAppender = (Appender) objLoggingAppender;
                        consoleAppender.setLayout(layout);
                    }
                } else {
                    consoleAppender = new ConsoleAppender(layout);
                }
                objRootLog.addAppender(consoleAppender);
                objRootLog.setLevel(Level.INFO);
                objRootLog.debug("Log4J configured programmatically");
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        if (conClassName.equals(pstrParentClassName)) {
            objCurrentLog = objRootLog;
        } else {
            objCurrentLog = Logger.getLogger(strParentClassName);
        }
        if (flgNew) {
            objRootLog.warn("log4j-property-file '" + objLog4JPropertyFile.getAbsolutePath() + "' does not exist - a default-file was created");
            objRootLog.debug("using log4j-property-file " + objLog4JPropertyFile.getAbsolutePath());
            objRootLog.warn("all log-entries will be written to the console");
        }
        objLevel = objCurrentLog.getLevel();
        return Logger.getRootLogger();
    }

}
