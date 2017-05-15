package com.sos.hibernate.exceptions;

import org.hibernate.HibernateException;

public class SOSHibernateTransactionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateTransactionException(String msg) {
        super(msg);
    }

    public SOSHibernateTransactionException(HibernateException cause) {
        super(cause);
    }
}
