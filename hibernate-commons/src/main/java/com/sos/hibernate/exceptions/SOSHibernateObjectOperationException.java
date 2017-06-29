package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

/** can occurs if following methods are called: delete, get, refresh, save, saveOrUpdate, update */
public class SOSHibernateObjectOperationException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateObjectOperationException(IllegalStateException cause) {
        super(cause);
    }

    public SOSHibernateObjectOperationException(PersistenceException cause) {
        super(cause);
    }

    public SOSHibernateObjectOperationException(String msg) {
        super(msg);
    }

    public SOSHibernateObjectOperationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
