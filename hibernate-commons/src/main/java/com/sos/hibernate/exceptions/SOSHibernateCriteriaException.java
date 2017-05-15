package com.sos.hibernate.exceptions;

import javax.persistence.PersistenceException;

public class SOSHibernateCriteriaException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateCriteriaException(String msg) {
        super(msg);
    }

    public SOSHibernateCriteriaException(PersistenceException cause) {
        super(cause);
    }
}
