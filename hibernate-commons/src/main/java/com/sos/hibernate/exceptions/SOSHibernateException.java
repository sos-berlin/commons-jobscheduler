package com.sos.hibernate.exceptions;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.JDBCException;

import com.sos.exception.SOSException;

public class SOSHibernateException extends SOSException {

    private static final long serialVersionUID = 1L;
    private String message = null;
    private SQLException sqlException = null;
    private String sqlStatement = null;

    public SOSHibernateException(String msg) {
        message = msg;
    }

    public SOSHibernateException(String msg, Throwable cause) {
        message = msg;
        initCause(cause);
    }

    public SOSHibernateException(HibernateException cause) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof JDBCException) {
                JDBCException je = (JDBCException) e;

                initCause(je);
                message = je.getMessage();
                sqlException = je.getSQLException();
                sqlStatement = je.getSQL();

                if (sqlException != null && sqlException.getMessage() != null) {
                    message = String.format("%s: %d %s", message, sqlException.getErrorCode(), sqlException.getMessage());
                }
                return;
            }
            e = e.getCause();
        }
        initCause(cause);
        message = cause.getMessage();
    }

    public SOSHibernateException(SQLException cause) {
        initCause(cause);
        sqlException = cause;
        message = String.format("%d %s", sqlException.getErrorCode(), sqlException.getMessage());
    }

    public SQLException getSqlException() {
        return sqlException;
    }

    public String getSqlStatement() {
        return sqlStatement;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
