package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

public class SOSHibernateTransactionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateTransactionException(String msg) {
        super(msg);
    }

    public SOSHibernateTransactionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SOSHibernateTransactionException(PersistenceException cause) {
        super(cause);
    }
}
