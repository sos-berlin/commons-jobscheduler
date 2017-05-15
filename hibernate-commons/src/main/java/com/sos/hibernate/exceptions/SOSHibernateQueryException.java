package com.sos.hibernate.exceptions;

import org.hibernate.HibernateException;

public class SOSHibernateQueryException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateQueryException(String msg) {
        super(msg);
    }

    public SOSHibernateQueryException(HibernateException cause) {
        super(cause);
    }
}
