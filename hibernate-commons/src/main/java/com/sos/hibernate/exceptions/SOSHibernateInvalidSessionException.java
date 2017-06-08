package com.sos.hibernate.exceptions;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import org.hibernate.query.Query;

/** occurs if session/connection can't be acquired (for example, database is not running) or session object is NULL */
public class SOSHibernateInvalidSessionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateInvalidSessionException(String msg) {
        super(msg);
    }

    public SOSHibernateInvalidSessionException(String msg, Query<?> query) {
        super(msg, query);
    }

    public SOSHibernateInvalidSessionException(String msg, String stmt) {
        super(msg, stmt);
    }

    public SOSHibernateInvalidSessionException(IllegalStateException cause) {
        super(cause);
    }

    public SOSHibernateInvalidSessionException(IllegalStateException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateInvalidSessionException(IllegalStateException cause, Query<?> query) {
        super(cause, query);
    }

    public SOSHibernateInvalidSessionException(PersistenceException cause) {
        super(cause);
    }

    public SOSHibernateInvalidSessionException(PersistenceException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateInvalidSessionException(SQLException cause, String stmt) {
        super(cause, stmt);
    }
}
