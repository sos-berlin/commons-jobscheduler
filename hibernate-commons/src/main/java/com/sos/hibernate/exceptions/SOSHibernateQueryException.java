package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

import org.hibernate.query.Query;

public class SOSHibernateQueryException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateQueryException(Throwable cause, Query<?> query) {
        super(cause, query);
    }

    public SOSHibernateQueryException(IllegalArgumentException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateQueryException(PersistenceException cause) {
        super(cause);
    }
}
