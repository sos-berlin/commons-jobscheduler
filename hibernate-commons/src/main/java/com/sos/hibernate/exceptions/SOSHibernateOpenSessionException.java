package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

public class SOSHibernateOpenSessionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateOpenSessionException(PersistenceException cause) {
        super(cause);
    }
}
