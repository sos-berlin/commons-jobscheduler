package com.sos.hibernate.exceptions;

public class SOSHibernateCriteriaException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateCriteriaException(String msg) {
        super(msg);
    }

    public SOSHibernateCriteriaException(Throwable cause) {
        super(cause);
    }
}
