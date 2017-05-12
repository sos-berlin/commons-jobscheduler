package com.sos.hibernate.exceptions;

public class SOSHibernateQueryException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateQueryException(String msg) {
        super(msg);
    }

    public SOSHibernateQueryException(Throwable cause) {
        super(cause);
    }
}
