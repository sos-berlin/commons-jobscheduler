package com.sos.hibernate.exceptions;

import org.hibernate.HibernateException;

public class SOSHibernateFactoryBuildException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateFactoryBuildException(HibernateException cause) {
        super(cause);
    }
}
