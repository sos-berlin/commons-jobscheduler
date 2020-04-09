package com.sos.scheduler.model.messages;

import com.sos.localization.SOSMsg;

public class JSMsg extends SOSMsg {

    private static int VERBOSITY_LEVEL = 0;

    public JSMsg(final String code) {
        super(code);

        if (getMessages() == null) {
            super.setMessageResource("com_sos_scheduler_model");
        }

        setVerbosityLevel(VERBOSITY_LEVEL);
        setCurVerbosityLevel(VERBOSITY_LEVEL);
    }

    public JSMsg(final String code, final int level) {
        this(code);
        setVerbosityLevel(level);
    }

    @Override
    protected void checkVerbosityLevel() {
        setCurVerbosityLevel(VERBOSITY_LEVEL);
    }
}
