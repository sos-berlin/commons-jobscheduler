package com.jcraft.jsch;

public class SOSRequiredAuthPublicKey extends SOSRequiredAuth {

    public SOSRequiredAuthPublicKey() throws Exception {
        super(JSCH_AUTH_CLASS_PUBLIC_KEY);
    }
}
