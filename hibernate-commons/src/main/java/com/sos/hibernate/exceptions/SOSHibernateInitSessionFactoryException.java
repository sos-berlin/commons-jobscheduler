package com.sos.hibernate.exceptions;

import org.hibernate.HibernateException;

public class SOSHibernateInitSessionFactoryException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateInitSessionFactoryException(HibernateException cause) {
        super(cause);
    }
}
