package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

public class SOSHibernateSessionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateSessionException(IllegalStateException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateSessionException(PersistenceException cause) {
        super(cause);
    }
}
