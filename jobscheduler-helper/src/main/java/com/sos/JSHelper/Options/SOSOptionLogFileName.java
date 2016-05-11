package com.sos.JSHelper.Options;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Logging.SOSHtmlLayout;
import com.sos.JSHelper.io.Files.JSTextFile;

public class SOSOptionLogFileName extends SOSOptionOutFileName {

    private static final long serialVersionUID = 144340120069043974L;
    private static Logger logger = Logger.getLogger(SOSOptionLogFileName.class);
    private FileAppender objFileAppender = null;
    private String strHtmlLogFile = "";
    public String ControlType = "file";

    public SOSOptionLogFileName(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    public String getHtmlLogFileName() {
        if (!isEmpty(strHtmlLogFile)) {
            return strHtmlLogFile;
        } else {
            return "";
        }
    }

    public void resetHTMLEntities() {
        if (isNotEmpty(strHtmlLogFile) == true && objFileAppender != null) {
            objFileAppender.close();
            JSTextFile objF = new JSTextFile(strHtmlLogFile);
            try {
                objF.replaceString("&lt;", "<");
                objF.replaceString("&gt;", ">");
            } catch (IOException e) {
                //
            }
        }
    }

    public String getContent() {
        String strContent = "";
        return strContent;
    }

    public void setLogger(final Logger pobjLogger) {
        if (pobjLogger != null && this.isDirty()) {
            try {
                logger = pobjLogger;
                @SuppressWarnings("rawtypes")
                Enumeration appenders = pobjLogger.getAllAppenders();
                objFileAppender = null;
                while (appenders.hasMoreElements()) {
                    Appender currAppender = (Appender) appenders.nextElement();
                    if (currAppender != null) {
                        if (currAppender instanceof FileAppender || currAppender instanceof RollingFileAppender) {
                            objFileAppender = (FileAppender) currAppender;
                            if (objFileAppender != null) {
                                String strLogFileName = this.Value();
                                if (objFileAppender.getLayout() instanceof SOSHtmlLayout) {
                                    if (isNotNull(objParentClass)) {
                                        /** This is a dirty trick: get the
                                         * optionname by name will check, wether
                                         * the option is present. if not, the
                                         * title will not changed This coding
                                         * below, with profile and settings, is
                                         * for JADE */
                                        String strProfile = objParentClass.OptionByName("profile");
                                        if (isNotEmpty(strProfile)) {
                                            String strSettings = objParentClass.OptionByName("settings");
                                            if (isNotEmpty(strSettings)) {
                                                strSettings += ":";
                                            } else {
                                                strSettings = "";
                                            }
                                            SOSHtmlLayout objLayout = (SOSHtmlLayout) objFileAppender.getLayout();
                                            String strTitle = objLayout.getTitle();
                                            objLayout.setTitle("[" + strSettings + strProfile + "] - " + strTitle);

                                        }
                                    }
                                    strLogFileName = strLogFileName + ".html";
                                    objFileAppender.setFile(strLogFileName);
                                    logger.debug(Messages.getMsg("%2$s: filename changed to '%1$s'", strLogFileName, "log4J.HTMLLayout"));
                                    strHtmlLogFile = strLogFileName;
                                } else {
                                    objFileAppender.setFile(strLogFileName);
                                    logger.debug(Messages.getMsg("%2$s: filename changed to '%1$s'", strLogFileName, "log4J.FileAppender"));
                                }
                                objFileAppender.activateOptions();
                            }
                        }
                    }
                }
                if (objFileAppender == null) {
                    logger.info("No File Appender found");
                }
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
                throw new JobSchedulerException("Problems with log4jappender", e);
            }
        } else {
            logger.trace("setLogger without instance of logger called.");
        }
    }

}