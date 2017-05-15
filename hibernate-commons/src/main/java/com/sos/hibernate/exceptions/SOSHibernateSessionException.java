package com.sos.hibernate.exceptions;

import org.hibernate.HibernateException;

public class SOSHibernateSessionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateSessionException(String msg) {
        super(msg);
    }

    public SOSHibernateSessionException(HibernateException cause) {
        super(cause);
    }
}
