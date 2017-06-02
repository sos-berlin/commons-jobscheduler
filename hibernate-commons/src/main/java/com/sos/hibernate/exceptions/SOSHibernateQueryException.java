package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

import org.hibernate.query.Query;

/** can occurs if Query/NativeQuery methods are called */
public class SOSHibernateQueryException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateQueryException(IllegalStateException cause, Query<?> query) {
        super(cause, query);
    }

    public SOSHibernateQueryException(IllegalStateException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateQueryException(IllegalArgumentException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateQueryException(PersistenceException cause) {
        super(cause);
    }

    public SOSHibernateQueryException(PersistenceException cause, Query<?> query) {
        super(cause, query);
    }

    public SOSHibernateQueryException(PersistenceException cause, String stmt) {
        super(cause, stmt);
    }
}
