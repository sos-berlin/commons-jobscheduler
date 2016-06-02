package com.sos.localization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.JSHelper.io.Files.JSTextFile;

/** @author JS */
public class SOSMsg {

    protected static final Logger logger = Logger.getLogger(SOSMsg.class);
    protected String strLastMsgKey = "";
    protected String strControlName = "";
    protected String strControlParent = "";
    protected int intVerbosityLevel = 0;
    protected enuMsgTypes enuMsgType = enuMsgTypes.undefined;
    protected int curVerbosityLevel = 0;
    protected String strMessageCode = null;
    protected Messages Messages = null;
    protected boolean flgFullMessageReported = false;
    private static final String PROPERTY_EXTENSION_F1 = ".f1";
    private static JSTextFile objMissingCodesPropertiesFile = null;
    private static HashMap<String, String> objMC = new HashMap<String, String>();
    public static final String conEnvironmentVariableSOS_LOCALE = "SOS_LOCALE";
    public static final String conPropertyExtensionTOOLTIP = ".tooltip";
    public static final String conPropertyExtensionShortText = ".shorttext";
    public static final String conPropertyExtensionLABEL = ".label";
    public static final String conPropertyExtensionICon = ".icon";
    public static final String conPropertyExtensionAccelerator = ".acc";
    public static int VerbosityLevel = 0;
    public static boolean flgShowFullMessageText = false;

    public enum enuMsgTypes {
        undefined, error, info, fatal, debug, warning, text, trace, label;
    }

    public SOSMsg(final String pstrMessageCode) {
        strMessageCode = pstrMessageCode;
        setMsgType();
    }

    public SOSMsg(final String pstrMessageCode, final int pintVerbosityLevel) {
        this(pstrMessageCode);
        intVerbosityLevel = pintVerbosityLevel;
    }

    public void toLog() {
        write2Log(this.get());
    }

    private void setMsgType() {
        enuMsgType = enuMsgTypes.undefined;
        if (strMessageCode.indexOf("_E_") != -1) {
            enuMsgType = enuMsgTypes.error;
        } else if (strMessageCode.indexOf("_F_") != -1) {
            enuMsgType = enuMsgTypes.fatal;
        } else if (strMessageCode.indexOf("_D_") != -1) {
            enuMsgType = enuMsgTypes.debug;
        } else if (strMessageCode.indexOf("_I_") != -1) {
            enuMsgType = enuMsgTypes.info;
        } else if (strMessageCode.indexOf("_W_") != -1) {
            enuMsgType = enuMsgTypes.warning;
        } else if (strMessageCode.indexOf("_T_") != -1) {
            enuMsgType = enuMsgTypes.text;
        } else if (strMessageCode.indexOf("_L_") != -1) {
            enuMsgType = enuMsgTypes.label;
        } else if (strMessageCode.indexOf("_R_") != -1) {
            enuMsgType = enuMsgTypes.trace;
        }
    }

    public void toLog(final Object... pstrArgs) {
        String strT = Messages.getMsg(strMessageCode, pstrArgs);
        strT = getFullMessage(strT);
        write2Log(strT);
    }

    private void write2Log(final String pstrLogMsg) {
        switch (enuMsgType) {
        case error:
            logger.error(pstrLogMsg);
            break;
        case fatal:
            logger.fatal(pstrLogMsg);
            throw new JobSchedulerException(pstrLogMsg);
        case warning:
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn(pstrLogMsg);
            }
            break;
        case debug:
            if (logger.isDebugEnabled()) {
                checkVerbosityLevel();
                if (intVerbosityLevel <= curVerbosityLevel) {
                    logger.debug(pstrLogMsg);
                }
            }
            break;
        case info:
        case text:
        case undefined:
            logger.info(pstrLogMsg);
            break;
        case trace:
            logger.trace(pstrLogMsg);
            break;
        default:
            logger.info(pstrLogMsg);
            break;
        }
    }

    public String icon() {
        String key = strMessageCode.trim().replaceAll(" ", "");
        String strI = Messages.getLabel(key + conPropertyExtensionICon);
        if (strI == null) {
            strI = "noIcon";
        }
        return strI;
    }

    public String accelerator() {
        String key = strMessageCode.trim().replaceAll(" ", "");
        String strI = Messages.getLabel(key + conPropertyExtensionAccelerator);
        if (strI == null) {
            strI = "";
        }
        return strI;
    }

    private String getLabel(final String pstrDefaultValue) {
        String key = strMessageCode.trim().replaceAll(" ", "");
        try {
            strLastMsgKey = key;
            String msg = Messages.getLabel(key + conPropertyExtensionLABEL);
            if (msg == null) {
                msg = Messages.getLabel(key + ".Label");
                if (msg == null) {
                    msg = Messages.getLabel(key.toLowerCase() + conPropertyExtensionLABEL);
                    if (msg == null) {
                        msg = Messages.getLabel(key.toLowerCase() + ".Label");
                        if (msg == null) {
                            msg = Messages.getLabel(key);
                        }
                    }
                }
            }
            String strT = strMessageCode;
            if (msg == null) {
                strT = "??" + strMessageCode + "??";
                strT = pstrDefaultValue;
                handleMissingCodes();
            } else {
                strT = msg;
            }
            return strT;
        } catch (Exception e) {
            return strMessageCode;
        }
    }

    private void handleMissingCodes() {
        if (objMissingCodesPropertiesFile == null) {
            objMissingCodesPropertiesFile = new JSTextFile(JSFile.getTempdir(), "MissingCodes.properties");
        }
        if (objMC.get(strMessageCode) == null) {
            try {
                objMissingCodesPropertiesFile.writeLine(strMessageCode + ".label = " + strMessageCode);
                objMissingCodesPropertiesFile.writeLine(strMessageCode + ".tooltip = " + strMessageCode);
                objMissingCodesPropertiesFile.writeLine(strMessageCode + ".shorttext = " + strMessageCode);
                objMissingCodesPropertiesFile.writeLine(strMessageCode + ".F1 = ");
                objMissingCodesPropertiesFile.writeLine(strMessageCode + ".F10 = ");
                objMissingCodesPropertiesFile.writeLine(strMessageCode + ".icon = ");
                objMissingCodesPropertiesFile.writeLine(strMessageCode + ".acc = ");
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            objMC.put(strMessageCode, "");
        }
    }

    public String label() {
        String key = strMessageCode.trim().replaceAll(" ", "");
        return getLabel(key);
    }

    public String caption() {
        return getLabel("");
    }

    public String tooltip() {
        try {
            strLastMsgKey = strMessageCode;
            String msg = Messages.getTooltip(strMessageCode + conPropertyExtensionTOOLTIP);
            if (msg == null) {
                msg = Messages.getTooltip("tooltip." + strMessageCode.toLowerCase());
                if (msg == null) {
                    msg = Messages.getTooltip(strMessageCode.toLowerCase() + conPropertyExtensionTOOLTIP);
                    if (msg == null) {
                        msg = Messages.getTooltip("tooltip." + strMessageCode);
                        if (msg == null) {
                            msg = Messages.getTooltip(strMessageCode + ".Tooltip");
                            if (msg == null) {
                                msg = Messages.getTooltip("Tooltip." + strMessageCode.toLowerCase());
                                if (msg == null) {
                                    msg = Messages.getTooltip(strMessageCode.toLowerCase() + ".Tooltip");
                                    if (msg == null) {
                                        msg = Messages.getTooltip("Tooltip." + strMessageCode);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if ("none".equals(msg.trim())) {
                msg = strMessageCode + ": sorry, no tooltip available yet";
            }
            String strLoc = strControlParent + "." + strControlName;
            if (!".".equals(strLoc)) {
                msg = strLoc + " -> " + msg;
            }
            return msg;
        } catch (Exception e) {
            return strMessageCode;
        }
    }

    public String getF1() {
        String key = strMessageCode.trim().replaceAll(" ", "");
        try {
            String msg = Messages.getLabel(key + ".F1");
            if (msg == null) {
                msg = Messages.getLabel(key.toLowerCase() + ".F1");
                if (msg == null) {
                    msg = Messages.getLabel(key + PROPERTY_EXTENSION_F1);
                    if (msg == null) {
                        msg = Messages.getLabel(key.toLowerCase() + PROPERTY_EXTENSION_F1);
                        if (msg == null) {
                            msg = Messages.getLabel(key);
                        }
                    }
                }
            }
            return msg != null && !"".equals(msg) ? msg : key;
        } catch (Exception e) {
            return strMessageCode;
        }
    }

    public String get() {
        String strT = "";
        if (!flgFullMessageReported) {
            strT = getFullMessage();
        } else {
            strT = Messages.getMsg(strMessageCode);
        }
        return strT;
    }

    public String get(final Exception pobjEx) {
        String strT = "";
        String strM = pobjEx.getMessage();
        if (!flgFullMessageReported) {
            strT = getFullMessage();
        } else {
            strT = Messages.getMsg(strMessageCode, strM);
        }
        return strT;
    }

    public String getFullMessage() {
        String strMsgText = Messages.getMsg(strMessageCode);
        return getFullMessage(strMsgText);
    }

    private String getFullMessage(final String pstrMsgText) {
        String strT = pstrMsgText;
        if (flgShowFullMessageText && !flgFullMessageReported) {
            switch (enuMsgType) {
            case error:
            case fatal:
            case warning:
                String strMsgDesc = Messages.getLabel(strMessageCode + ".description");
                if (strMsgDesc == null) {
                    strMsgDesc = "*** no detailed description available ***";
                }
                String strMsgReason = Messages.getLabel(strMessageCode + ".reason");
                if (strMsgReason == null) {
                    strMsgReason = "*** no detailed explanation available ***";
                }
                strT = pstrMsgText + "\n\n" + "DESCRIPTION" + "\n" + strMsgDesc + "\n\n" + "REASON" + "\n" + strMsgReason;
                flgFullMessageReported = true;
                break;
            default:
                break;
            }
        }
        return strT;
    }

    public String get(final Object... pstrArgs) {
        return getFullMessage(Messages.getMsg(strMessageCode, pstrArgs));
    }

    public String getFullMessage(final Object... pstrArgs) {
        boolean flgT = flgShowFullMessageText;
        flgShowFullMessageText = true;
        String strT = Messages.getMsg(strMessageCode, pstrArgs);
        strT = getFullMessage(strT);
        strT = String.format(strT, pstrArgs);
        flgShowFullMessageText = flgT;
        return strT;
    }

    public String params(final Object... pstrArgs) {
        String msg = Messages.getLabel(strMessageCode + conPropertyExtensionLABEL);
        if (msg == null) {
            msg = Messages.getLabel(strMessageCode.toLowerCase() + conPropertyExtensionLABEL);
        }
        if (msg != null) {
            try {
                msg = String.format(msg, pstrArgs);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            msg = Messages.getMsg(strMessageCode, pstrArgs);
        }
        return msg;
    }

    public String params(final String[] pstrArgs) {
        String msg = Messages.getLabel(strMessageCode + conPropertyExtensionLABEL);
        if (msg == null) {
            msg = Messages.getLabel(strMessageCode.toLowerCase() + conPropertyExtensionLABEL);
        }
        String strT = "";
        for (String string : pstrArgs) {
            strT += " " + string;
        }
        if (msg != null) {
            msg = String.format(msg, strT);
        } else {
            msg = Messages.getMsg(strMessageCode, strT);
        }
        return msg;
    }

    public String paramsNoKey(final Object... pstrArgs) {
        return String.format(Messages.getLabel(strMessageCode), pstrArgs);
    }

    protected void setMessageResource(final String pstrResourceBundleName) {
        String strSOSLocale = System.getenv(conEnvironmentVariableSOS_LOCALE);
        if (strSOSLocale == null) {
            Messages = new Messages(pstrResourceBundleName, Locale.getDefault());
        } else {
            Messages = new Messages(pstrResourceBundleName, new Locale(strSOSLocale));
        }
    }

    protected void setMessageResource(final String pstrResourceBundleName, final String pstrLocale) {
        Messages = new Messages(pstrResourceBundleName, new Locale(pstrLocale));
    }

    protected void checkVerbosityLevel() {
        //
    }

}