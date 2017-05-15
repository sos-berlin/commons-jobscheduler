package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

import org.hibernate.query.Query;

public class SOSHibernateQueryException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateQueryException(String msg) {
        super(msg);
    }

    public SOSHibernateQueryException(Query<?> query, Throwable cause) {
        super(query, cause);
    }

    public SOSHibernateQueryException(IllegalArgumentException cause) {
        super(cause);
    }

    public SOSHibernateQueryException(IllegalStateException cause) {
        super(cause);
    }

    public SOSHibernateQueryException(PersistenceException cause) {
        super(cause);
    }
}
