package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

/** can occurs if following methods are called: clearSession, delete, get, refresh, save, saveOrUpdate, update, sessionDoWork */
public class SOSHibernateSessionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateSessionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SOSHibernateSessionException(IllegalStateException cause) {
        super(cause);
    }

    public SOSHibernateSessionException(IllegalStateException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateSessionException(PersistenceException cause) {
        super(cause);
    }
}
