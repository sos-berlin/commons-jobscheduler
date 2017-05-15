package com.sos.hibernate.exceptions;

import org.hibernate.HibernateException;

public class SOSHibernateConfigurationException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateConfigurationException(String msg) {
        super(msg);
    }

    public SOSHibernateConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SOSHibernateConfigurationException(HibernateException cause) {
        super(cause);
    }
}
