package com.sos.hibernate.exceptions;

import org.hibernate.HibernateException;

public class SOSHibernateConnectionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateConnectionException(HibernateException cause) {
        super(cause);
    }
}
