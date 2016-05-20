package com.sos.JSHelper.Listener;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JSListenerClass implements JSListener {


    protected JSListener JSListener = null;
    private static final Logger LOGGER = Logger.getLogger(JSListenerClass.class);
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

    public void message(final String pstrMsg) {
        String strT = pstrMsg;
        strT = getTime() + " " + strT;
        if (JSListener != null) {
            JSListener.message(strT);
        } else {
            LOGGER.info(strT);
        }
    }

    public JSListener getListener() {
        return JSListener;
    }

    public void registerMessageListener(final JSListener l) {
        JSListener = l;
    }

    public void signalAbort(final String strS) throws Exception {
        String strT = " ###ProgramAbort### ";
        strT = strT + strS + strT;
        message(strT);
        final JobSchedulerException expE = new JobSchedulerException(strT);
        throw expE;
    }

    public void signalInfo(final String strS) {
        message(strS);
    }

    public void signalError(final JobSchedulerException expE, final String strS) {
        String strT = " ### Error ### ";
        strT = strT + strS + strT;
        message(strT);
        if (expE != null) {
            expE.message(strS);
            expE.setStatus(JobSchedulerException.ERROR);
            throw expE;
        }
    }

    public void signalError(final String strS) {
        this.signalError(new JobSchedulerException(strS), strS);
    }

    public void signalDebug(final String pstrDebugMessage) {
        this.signalDebug(pstrDebugMessage, 5);
    }

    public void signalDebug(final String pstrMsg, final Integer pintDebugLevel) {
        final String strT = pstrMsg;
        if (!JSListenerClass.bolLogDebugInformation) {
            return;
        }
        if (intMaxDebugLevel == null) {
            intMaxDebugLevel = JSListenerClass.DEBUG_LEVEL9;
        }
        if (pintDebugLevel.intValue() > JSListenerClass.intMaxDebugLevel.intValue()) {
            return;
        }
        message("DEBUG(" + pintDebugLevel + "):>>> " + strT + " <<<");
    }

    public String getDateTimeFormatted(final String pstrEditMask) {
        final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(pstrEditMask);
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