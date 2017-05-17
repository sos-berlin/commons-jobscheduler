package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

public class SOSHibernateCriteriaException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateCriteriaException(String msg) {
        super(msg);
    }

    public SOSHibernateCriteriaException(IllegalStateException cause) {
        super(cause);
    }

    public SOSHibernateCriteriaException(IllegalStateException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateCriteriaException(PersistenceException cause) {
        super(cause);
    }
}
