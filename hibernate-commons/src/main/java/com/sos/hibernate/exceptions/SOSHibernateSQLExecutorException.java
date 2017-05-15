package com.sos.hibernate.exceptions;

import java.sql.SQLException;

public class SOSHibernateSQLExecutorException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateSQLExecutorException(String msg) {
        super(msg);
    }

    public SOSHibernateSQLExecutorException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SOSHibernateSQLExecutorException(SQLException cause, String sql) {
        super(cause,sql);
    }
}
