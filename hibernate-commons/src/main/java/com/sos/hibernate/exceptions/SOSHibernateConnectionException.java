package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

/** can occurs if JDBC connection methods are called */
public class SOSHibernateConnectionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateConnectionException(String msg) {
        super(msg);
    }

    public SOSHibernateConnectionException(IllegalStateException cause) {
        super(cause);
    }

    public SOSHibernateConnectionException(IllegalStateException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateConnectionException(PersistenceException cause) {
        super(cause);
    }
}
