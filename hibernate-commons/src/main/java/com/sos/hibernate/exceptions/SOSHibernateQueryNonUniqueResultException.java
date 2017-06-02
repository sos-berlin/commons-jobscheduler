package com.sos.hibernate.exceptions;

import org.hibernate.NonUniqueResultException;
import org.hibernate.query.Query;

/** thrown if query returned more than one result */
public class SOSHibernateQueryNonUniqueResultException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateQueryNonUniqueResultException(String msg, Query<?> query) {
        super(msg, query);
    }

    public SOSHibernateQueryNonUniqueResultException(NonUniqueResultException cause, Query<?> query) {
        super(cause.getMessage(), query);
        initCause(cause);
    }
}
