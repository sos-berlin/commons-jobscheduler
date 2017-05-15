package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

public class SOSHibernateInitSessionFactoryException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateInitSessionFactoryException(PersistenceException cause) {
        super(cause);
    }
}
