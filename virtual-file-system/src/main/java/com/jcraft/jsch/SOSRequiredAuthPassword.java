package com.jcraft.jsch;

public class SOSRequiredAuthPassword extends SOSRequiredAuth {

    public SOSRequiredAuthPassword() throws Exception {
        super(JSCH_AUTH_CLASS_PASSWORD);
    }
}
