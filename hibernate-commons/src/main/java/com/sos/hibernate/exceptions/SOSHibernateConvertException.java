package com.sos.hibernate.exceptions;

/** can occurs if factory.quote() method are called */
public class SOSHibernateConvertException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateConvertException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
