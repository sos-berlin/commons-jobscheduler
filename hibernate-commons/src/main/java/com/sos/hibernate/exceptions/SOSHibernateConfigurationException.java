package com.sos.hibernate.exceptions;

public class SOSHibernateConfigurationException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateConfigurationException(String msg) {
        super(msg);
    }

    public SOSHibernateConfigurationException(Throwable cause) {
        super(cause);
    }
}
