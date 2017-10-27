package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

/** can occurs if following methods are called: delete, get, refresh, save, saveOrUpdate, update */
public class SOSHibernateObjectOperationException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateObjectOperationException(IllegalStateException cause, Object item) {
        super(cause);
        setDbItem(item);
    }

    public SOSHibernateObjectOperationException(PersistenceException cause, Object item) {
        super(cause);
        setDbItem(item);
    }

    public SOSHibernateObjectOperationException(String msg, Object item) {
        super(msg);
        setDbItem(item);
    }

    public SOSHibernateObjectOperationException(String msg, Throwable cause, Object item) {
        super(msg, cause);
        setDbItem(item);
    }
}
