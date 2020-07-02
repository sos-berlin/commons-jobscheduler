package com.jcraft.jsch;

public class SOSRequiredAuthKeyboardInteractive extends SOSRequiredAuth {

    public SOSRequiredAuthKeyboardInteractive() throws Exception {
        super(JSCH_AUTH_CLASS_KEYBOARD_INTERACTIVE);
    }
}
