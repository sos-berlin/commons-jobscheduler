package com.sos.localization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.JSHelper.io.Files.JSTextFile;

public class SOSMsg {

    private static final Logger lOGGER = LoggerFactory.getLogger(SOSMsg.class);

    private static JSTextFile MISSING_CODES_FILE = null;
    private static HashMap<String, String> CODES = new HashMap<String, String>();
    private static final String PROPERTY_EXTENSION_F1 = ".f1";
    private static final String ENV_VAR_SOS_LOCALE = "SOS_LOCALE";
    private static final String EXTENSION_TOOLTIP = ".tooltip";
    private static final String EXTENSION_LABEL = ".label";
    private static final String EXTENSION_ICON = ".icon";
    private static final String EXTENSION_ACC = ".acc";
    private static boolean SHOW_FULL_MESSAGE_TEXT = false;

    private enum MsgTypes {
        undefined, error, info, fatal, debug, warning, text, trace, label;
    }

    private MsgTypes type = MsgTypes.undefined;
    private Messages messages = null;
    private String controlName = "";
    private String controlParent = "";
    private int verbosityLevel = 0;
    private int curVerbosityLevel = 0;
    private String code = null;
    private boolean fullMessageReported = false;

    public SOSMsg(final String messageCode) {
        code = messageCode;
        setMsgType();
    }

    public SOSMsg(final String messageCode, final int level) {
        this(messageCode);
        verbosityLevel = level;
    }

    public void toLog() {
        write2Log(this.get());
    }

    private void setMsgType() {
        type = MsgTypes.undefined;
        if (code.indexOf("_E_") != -1) {
            type = MsgTypes.error;
        } else if (code.indexOf("_F_") != -1) {
            type = MsgTypes.fatal;
        } else if (code.indexOf("_D_") != -1) {
            type = MsgTypes.debug;
        } else if (code.indexOf("_I_") != -1) {
            type = MsgTypes.info;
        } else if (code.indexOf("_W_") != -1) {
            type = MsgTypes.warning;
        } else if (code.indexOf("_T_") != -1) {
            type = MsgTypes.text;
        } else if (code.indexOf("_L_") != -1) {
            type = MsgTypes.label;
        } else if (code.indexOf("_R_") != -1) {
            type = MsgTypes.trace;
        }
    }

    public void toLog(final Object... args) {
        write2Log(getFullMessage(messages.getMsg(code, args)));
    }

    private void write2Log(final String msg) {
        switch (type) {
        case error:
            lOGGER.error(msg);
            break;
        case fatal:
            lOGGER.error(msg);
            throw new JobSchedulerException(msg);
        case warning:
            if (lOGGER.isWarnEnabled()) {
                lOGGER.warn(msg);
            }
            break;
        case debug:
            if (lOGGER.isDebugEnabled()) {
                checkVerbosityLevel();
                if (verbosityLevel <= curVerbosityLevel) {
                    lOGGER.debug(msg);
                }
            }
            break;
        case info:
        case text:
        case undefined:
            lOGGER.info(msg);
            break;
        case trace:
            lOGGER.trace(msg);
            break;
        default:
            lOGGER.info(msg);
            break;
        }
    }

    public String icon() {
        String key = code.trim().replaceAll(" ", "");
        String label = messages.getLabel(key + EXTENSION_ICON);
        if (label == null) {
            label = "noIcon";
        }
        return label;
    }

    public String accelerator() {
        String key = code.trim().replaceAll(" ", "");
        String label = messages.getLabel(key + EXTENSION_ACC);
        if (label == null) {
            label = "";
        }
        return label;
    }

    private String getLabel(final String defaultValue) {
        String key = code.trim().replaceAll(" ", "");
        try {
            String msg = messages.getLabel(key + EXTENSION_LABEL);
            if (msg == null) {
                msg = messages.getLabel(key + ".Label");
                if (msg == null) {
                    msg = messages.getLabel(key.toLowerCase() + EXTENSION_LABEL);
                    if (msg == null) {
                        msg = messages.getLabel(key.toLowerCase() + ".Label");
                        if (msg == null) {
                            msg = messages.getLabel(key);
                        }
                    }
                }
            }
            String result = code;
            if (msg == null) {
                result = "??" + code + "??";
                result = defaultValue;
                handleMissingCodes();
            } else {
                result = msg;
            }
            return result;
        } catch (Exception e) {
            return code;
        }
    }

    private void handleMissingCodes() {
        if (MISSING_CODES_FILE == null) {
            MISSING_CODES_FILE = new JSTextFile(JSFile.getTempdir(), "MissingCodes.properties");
        }
        if (CODES.get(code) == null) {
            try {
                MISSING_CODES_FILE.writeLine(code + ".label = " + code);
                MISSING_CODES_FILE.writeLine(code + ".tooltip = " + code);
                MISSING_CODES_FILE.writeLine(code + ".shorttext = " + code);
                MISSING_CODES_FILE.writeLine(code + ".F1 = ");
                MISSING_CODES_FILE.writeLine(code + ".F10 = ");
                MISSING_CODES_FILE.writeLine(code + ".icon = ");
                MISSING_CODES_FILE.writeLine(code + ".acc = ");
            } catch (IOException e) {
                lOGGER.error(e.getMessage(), e);
            }
            CODES.put(code, "");
        }
    }

    public String label() {
        String key = code.trim().replaceAll(" ", "");
        return getLabel(key);
    }

    public String caption() {
        return getLabel("");
    }

    public String tooltip() {
        try {
            String msg = messages.getTooltip(code + EXTENSION_TOOLTIP);
            if (msg == null) {
                msg = messages.getTooltip("tooltip." + code.toLowerCase());
                if (msg == null) {
                    msg = messages.getTooltip(code.toLowerCase() + EXTENSION_TOOLTIP);
                    if (msg == null) {
                        msg = messages.getTooltip("tooltip." + code);
                        if (msg == null) {
                            msg = messages.getTooltip(code + ".Tooltip");
                            if (msg == null) {
                                msg = messages.getTooltip("Tooltip." + code.toLowerCase());
                                if (msg == null) {
                                    msg = messages.getTooltip(code.toLowerCase() + ".Tooltip");
                                    if (msg == null) {
                                        msg = messages.getTooltip("Tooltip." + code);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if ("none".equals(msg.trim())) {
                msg = code + ": sorry, no tooltip available yet";
            }
            String control = controlParent + "." + controlName;
            if (!".".equals(control)) {
                msg = control + " -> " + msg;
            }
            return msg;
        } catch (Exception e) {
            return code;
        }
    }

    public String getF1() {
        String key = code.trim().replaceAll(" ", "");
        try {
            String msg = messages.getLabel(key + ".F1");
            if (msg == null) {
                msg = messages.getLabel(key.toLowerCase() + ".F1");
                if (msg == null) {
                    msg = messages.getLabel(key + PROPERTY_EXTENSION_F1);
                    if (msg == null) {
                        msg = messages.getLabel(key.toLowerCase() + PROPERTY_EXTENSION_F1);
                        if (msg == null) {
                            msg = messages.getLabel(key);
                        }
                    }
                }
            }
            return msg != null && !"".equals(msg) ? msg : key;
        } catch (Exception e) {
            return code;
        }
    }

    public String get() {
        if (!fullMessageReported) {
            return getFullMessage();
        } else {
            return messages.getMsg(code);
        }
    }

    public String get(final Exception ex) {
        if (!fullMessageReported) {
            return getFullMessage();
        } else {
            return messages.getMsg(code, ex.getMessage());
        }
    }

    public String getFullMessage() {
        return getFullMessage(messages.getMsg(code));
    }

    private String getFullMessage(final String msg) {
        StringBuilder sb = new StringBuilder(msg);
        if (SHOW_FULL_MESSAGE_TEXT && !fullMessageReported) {
            switch (type) {
            case error:
            case fatal:
            case warning:
                String desc = messages.getLabel(code + ".description");
                if (desc == null) {
                    desc = "*** no detailed description available ***";
                }
                String reason = messages.getLabel(code + ".reason");
                if (reason == null) {
                    reason = "*** no detailed explanation available ***";
                }
                sb.append("\n\n").append("DESCRIPTION").append("\n").append(desc).append("\n\n").append("REASON").append("\n").append(reason);
                fullMessageReported = true;
                break;
            default:
                break;
            }
        }
        return sb.toString();
    }

    public String get(final Object... args) {
        return getFullMessage(messages.getMsg(code, args));
    }

    public String getFullMessage(final Object... args) {
        boolean full = SHOW_FULL_MESSAGE_TEXT;
        SHOW_FULL_MESSAGE_TEXT = true;
        String msg = messages.getMsg(code, args);
        msg = String.format(getFullMessage(msg), args);
        SHOW_FULL_MESSAGE_TEXT = full;
        return msg;
    }

    public String params(final Object... args) {
        String msg = messages.getLabel(code + EXTENSION_LABEL);
        if (msg == null) {
            msg = messages.getLabel(code.toLowerCase() + EXTENSION_LABEL);
        }
        if (msg != null) {
            try {
                msg = String.format(msg, args);
            } catch (Exception e) {
                lOGGER.error(e.getMessage(), e);
            }
        } else {
            msg = messages.getMsg(code, args);
        }
        return msg;
    }

    public String params(final String[] args) {
        String msg = messages.getLabel(code + EXTENSION_LABEL);
        if (msg == null) {
            msg = messages.getLabel(code.toLowerCase() + EXTENSION_LABEL);
        }
        StringBuilder sb = new StringBuilder();
        for (String string : args) {
            sb.append(" ").append(string);
        }
        if (msg != null) {
            msg = String.format(msg, sb.toString());
        } else {
            msg = messages.getMsg(code, sb.toString());
        }
        return msg;
    }

    public String paramsNoKey(final Object... args) {
        return String.format(messages.getLabel(code), args);
    }

    protected void setMessageResource(final String val) {
        String locale = System.getenv(ENV_VAR_SOS_LOCALE);
        if (locale == null) {
            messages = new Messages(val, Locale.getDefault());
        } else {
            messages = new Messages(val, new Locale(locale));
        }
    }

    protected void setMessageResource(final String bundleName, final String locale) {
        messages = new Messages(bundleName, new Locale(locale));
    }

    protected void checkVerbosityLevel() {
        //
    }

    public void setVerbosityLevel(int val) {
        verbosityLevel = val;
    }

    public void setCurVerbosityLevel(int val) {
        curVerbosityLevel = val;
    }

    public void setMessages(Messages val) {
        messages = val;
    }

    public Messages getMessages() {
        return messages;
    }
}