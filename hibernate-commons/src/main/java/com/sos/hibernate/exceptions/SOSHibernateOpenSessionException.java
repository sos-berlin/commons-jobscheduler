package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

/** can occurs if openSession() methods are called
 * 
 * occurs if session/connection can't be acquired (for example, database is not running)
 * 
 * not occurs with openSession() methods if c3p0 pool has been configured
 * 
 * if c3p0 pool has been configured and session/connection can't be acquired exception will be catched in the other methods like createQuery() etc as
 * SOSHibernateInvalidSessionException */
public class SOSHibernateOpenSessionException extends SOSHibernateInvalidSessionException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateOpenSessionException(IllegalStateException cause) {
        super(cause);
    }

    public SOSHibernateOpenSessionException(PersistenceException cause) {
        super(cause);
    }

    public SOSHibernateOpenSessionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
