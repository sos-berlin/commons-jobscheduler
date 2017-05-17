package com.sos.hibernate.exceptions;

import com.sos.exception.SOSException;

@Deprecated
public class SOSDBException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSDBException() {
        super();
    }

    public SOSDBException(String message) {
        super(message);
    }

    public SOSDBException(Throwable cause) {
        super(cause);
    }

    public SOSDBException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSDBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
