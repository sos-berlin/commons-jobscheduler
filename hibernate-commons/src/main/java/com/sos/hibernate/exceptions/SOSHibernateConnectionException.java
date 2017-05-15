package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

public class SOSHibernateConnectionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateConnectionException(PersistenceException cause) {
        super(cause);
    }
}
