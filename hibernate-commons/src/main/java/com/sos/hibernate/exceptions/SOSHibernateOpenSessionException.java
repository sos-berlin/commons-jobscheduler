package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

/** if session/connection can't be acquired (for example, database is not running), occur: */
/** - by openSession() methods if c3p0 not configured */
/** - if c3p0 configured - occur later in the next methods (for example, by createQuery ... as SOSHibernateQueryException) */
public class SOSHibernateOpenSessionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateOpenSessionException(PersistenceException cause) {
        super(cause);
    }

    public SOSHibernateOpenSessionException(IllegalStateException cause) {
        super(cause);
    }

    public SOSHibernateOpenSessionException(IllegalStateException cause, String stmt) {
        super(cause, stmt);
    }
}
