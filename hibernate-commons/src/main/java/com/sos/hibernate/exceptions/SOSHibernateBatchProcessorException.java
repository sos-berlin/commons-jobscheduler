package com.sos.hibernate.exceptions;

import java.sql.SQLException;

/** can occurs if SOSHibernateBatchProcessor methods are called */
public class SOSHibernateBatchProcessorException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateBatchProcessorException(String msg) {
        super(msg);
    }

    public SOSHibernateBatchProcessorException(String msg, Throwable e) {
        super(msg, e);
    }

    public SOSHibernateBatchProcessorException(SQLException cause, String sql) {
        super(cause, sql);
    }
}
