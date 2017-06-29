package com.sos.hibernate.exceptions;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import org.hibernate.query.Query;

/** lock wait timeout exceeded and deadlocks */
public class SOSHibernateLockAcquisitionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateLockAcquisitionException(PersistenceException cause) {
        super(cause);
    }

    public SOSHibernateLockAcquisitionException(PersistenceException cause, Query<?> query) {
        super(cause, query);
    }

    public SOSHibernateLockAcquisitionException(PersistenceException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateLockAcquisitionException(SQLException cause, String stmt) {
        super(cause, stmt);
    }

}
