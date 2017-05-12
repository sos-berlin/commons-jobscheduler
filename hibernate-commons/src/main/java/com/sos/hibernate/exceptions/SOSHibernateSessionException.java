package com.sos.hibernate.exceptions;

public class SOSHibernateSessionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateSessionException(String msg) {
        super(msg);
    }

    public SOSHibernateSessionException(Throwable cause) {
        super(cause);
    }
}
