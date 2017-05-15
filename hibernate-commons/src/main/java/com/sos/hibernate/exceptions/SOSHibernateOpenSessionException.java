package com.sos.hibernate.exceptions;

import org.hibernate.HibernateException;

public class SOSHibernateOpenSessionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateOpenSessionException(HibernateException cause) {
        super(cause);
    }
}
