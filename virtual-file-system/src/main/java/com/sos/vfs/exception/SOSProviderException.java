package com.sos.vfs.exception;

import com.sos.exception.SOSException;

public class SOSProviderException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSProviderException() {
        super();
    }

    public SOSProviderException(String msg) {
        super(msg);
    }
}
