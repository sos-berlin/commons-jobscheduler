package com.sos.hibernate.exceptions;

/** can occurs if SOSHibernateSQLCommandExtractor methods are called */
public class SOSHibernateSQLCommandExtractorException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateSQLCommandExtractorException(String msg) {
        super(msg);
    }
}
