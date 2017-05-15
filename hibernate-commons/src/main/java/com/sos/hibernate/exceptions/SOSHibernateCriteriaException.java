package com.sos.hibernate.exceptions;

import org.hibernate.HibernateException;

public class SOSHibernateCriteriaException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateCriteriaException(String msg) {
        super(msg);
    }

    public SOSHibernateCriteriaException(HibernateException cause) {
        super(cause);
    }
}
