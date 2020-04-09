package com.sos.JSHelper.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JSListenerClass implements JSListener {

    protected JSListener JSListener = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(JSListenerClass.class);
    public static final Integer DEBUG_LEVEL1 = new Integer(1);
    public static final Integer DEBUG_LEVEL2 = new Integer(2);
    public static final Integer DEBUG_LEVEL3 = new Integer(3);
    public static final Integer DEBUG_LEVEL4 = new Integer(4);
    public static final Integer DEBUG_LEVEL5 = new Integer(5);
    public static final Integer DEBUG_LEVEL6 = new Integer(6);
    public static final Integer DEBUG_LEVEL7 = new Integer(7);
    public static final Integer DEBUG_LEVEL8 = new Integer(8);
    public static final Integer DEBUG_LEVEL9 = new Integer(9);
    public static Integer intMaxDebugLevel = 0;
    public static boolean bolLogDebugInformation = true;
    public final String conClassName = "JSListenerClass";

    public JSListenerClass() {
        //
    }

    public void message(String msg) {
        StringBuilder sb = new StringBuilder(getTime()).append(" ").append(msg);
        if (JSListener != null) {
            JSListener.message(sb.toString());
        } else {
            LOGGER.info(sb.toString());
        }
    }

    public JSListener getListener() {
        return JSListener;
    }

    public void registerMessageListener(final JSListener l) {
        JSListener = l;
    }

    public void signalAbort(final String msg) throws Exception {
        String def = " ###ProgramAbort### ";
        String message = def + msg + def;
        message(message);
        throw new JobSchedulerException(message);
    }

    public void signalInfo(final String msg) {
        message(msg);
    }

    public void signalError(final JobSchedulerException ex, final String msg) {
        String def = " ### Error ### ";
        String message = def + msg + def;
        message(msg);
        if (ex != null) {
            ex.message(message);
            throw ex;
        }
    }

    public void signalError(final String msg) {
        this.signalError(new JobSchedulerException(msg), msg);
    }

    public void signalDebug(final String msg) {
        this.signalDebug(msg, 5);
    }

    public void signalDebug(final String msg, final Integer level) {
        if (!JSListenerClass.bolLogDebugInformation) {
            return;
        }
        if (intMaxDebugLevel == null) {
            intMaxDebugLevel = JSListenerClass.DEBUG_LEVEL9;
        }
        if (level.intValue() > JSListenerClass.intMaxDebugLevel.intValue()) {
            return;
        }
        message("DEBUG(" + level + "):>>> " + msg + " <<<");
    }

    public String getDateTimeFormatted(final String dateTime) {
        final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(dateTime);
        final java.util.Calendar now = java.util.Calendar.getInstance();
        return formatter.format(now.getTime()).toString();
    }

    public String getISODate() {
        return getDateTimeFormatted("yyyy-MM-dd'T'HH:mm:ss");
    }

    public String getTime() {
        return getDateTimeFormatted("HH:mm:ss");
    }

}