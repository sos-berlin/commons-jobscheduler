package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

public class SOSHibernateFactoryBuildException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateFactoryBuildException(PersistenceException cause) {
        super(cause);
    }

    public SOSHibernateFactoryBuildException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
