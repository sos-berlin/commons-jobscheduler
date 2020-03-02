package com.sos.scheduler.messages;

import com.sos.localization.SOSMsg;

public class JSMsg extends SOSMsg {

    private static int VERBOSITY_LEVEL = 0;

    public JSMsg(final String messageCode) {
        super(messageCode);

        if (getMessages() == null) {
            super.setMessageResource("com_sos_scheduler_messages");
        }

        setVerbosityLevel(VERBOSITY_LEVEL);
        setCurVerbosityLevel(VERBOSITY_LEVEL);
    }

    public JSMsg(final String messageCode, final int verbosityLevel) {
        this(messageCode);
        setVerbosityLevel(verbosityLevel);
    }

    @Override
    protected void checkVerbosityLevel() {
        setCurVerbosityLevel(VERBOSITY_LEVEL);
    }
}
