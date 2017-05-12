package com.sos.hibernate.exceptions;

public class SOSHibernateTransactionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateTransactionException(String msg) {
        super(msg);
    }

    public SOSHibernateTransactionException(Throwable cause) {
        super(cause);
    }
}
