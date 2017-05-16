package com.sos.hibernate.exceptions;

import org.hibernate.query.Query;

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

    public SOSHibernateInvalidSessionException(IllegalStateException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateInvalidSessionException(IllegalStateException cause, Query<?> query) {
        super(cause, query);
    }
}
