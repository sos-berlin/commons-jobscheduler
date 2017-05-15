package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

public class SOSHibernateConfigurationException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateConfigurationException(String msg) {
        super(msg);
    }

    public SOSHibernateConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SOSHibernateConfigurationException(PersistenceException cause) {
        super(cause);
    }
}
