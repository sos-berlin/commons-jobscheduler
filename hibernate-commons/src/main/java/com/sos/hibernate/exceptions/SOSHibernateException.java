package com.sos.hibernate.exceptions;

import java.sql.SQLException;

import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSException;

public class SOSHibernateException extends SOSException {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateException.class);
    private static final long serialVersionUID = 1L;
    private String message = null;
    private SQLException sqlException = null;
    private String sqlStatement = null;
    private boolean sqlStatementIsLogged = false;

    public SOSHibernateException(String msg) {
        message = msg;
    }

    public SOSHibernateException(Throwable cause) {
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

    @Override
    public String toString() {
        if (sqlStatement != null && !sqlStatementIsLogged) {
            LOGGER.info("sql statement: " + sqlStatement);
            sqlStatementIsLogged = true;
        }
        return super.toString();
    }
}
