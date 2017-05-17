package com.sos.hibernate.exceptions;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class SOSJDBCException extends SOSDBException {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSJDBCException.class);
    private static final long serialVersionUID = 1L;
    private SQLException sqlException = null;
    private String sqlStatement = null;
    private boolean sqlStatementIsLogged = false;
    
    public SOSJDBCException() {
        super();
    }

    public SOSJDBCException(String message) {
        super(message);
    }
    
    public SOSJDBCException(String message, String sqlStatement) {
        super(message);
        this.sqlStatement = sqlStatement;
    }
    
    public SOSJDBCException(SQLException sqlCause) {
        super(sqlCause);
        this.sqlException = sqlCause;
    }
    
    public SOSJDBCException(Throwable cause) {
        super(cause);
    }
    
    public SOSJDBCException(SQLException sqlCause, String sqlStatement) {
        super(sqlCause);
        this.sqlException = sqlCause;
        this.sqlStatement = sqlStatement;
    }
    
    public SOSJDBCException(Throwable cause, String sqlStatement) {
        super(cause);
        this.sqlStatement = sqlStatement;
    }
    
    public SOSJDBCException(SQLException sqlCause, Throwable cause) {
        super(cause);
        this.sqlException = sqlCause;
    }
    
    public SOSJDBCException(SQLException sqlCause, Throwable cause, String sqlStatement) {
        super(cause);
        this.sqlException = sqlCause;
        this.sqlStatement = sqlStatement;
    }
    
    public SOSJDBCException(String message, SQLException sqlCause) {
        super(message, sqlCause);
        this.sqlException = sqlCause;
    }
    
    public SOSJDBCException(String message, SQLException sqlCause, String sqlStatement) {
        super(message, sqlCause);
        this.sqlException = sqlCause;
        this.sqlStatement = sqlStatement;
    }
    
    public SOSJDBCException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SOSJDBCException(String message, Throwable cause, String sqlStatement) {
        super(message, cause);
        this.sqlStatement = sqlStatement;
    }
    
    public SOSJDBCException(String message, SQLException sqlCause, Throwable cause) {
        super(message, cause);
        this.sqlException = sqlCause;
    }
    
    public SOSJDBCException(String message, SQLException sqlCause, Throwable cause, String sqlStatement) {
        super(message, cause);
        this.sqlException = sqlCause;
        this.sqlStatement = sqlStatement;
    }
    
    public SOSJDBCException(String message, SQLException sqlCause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, sqlCause, enableSuppression, writableStackTrace);
        this.sqlException = sqlCause;
    }
    
    public SOSJDBCException(String message, SQLException sqlCause, String sqlStatement, boolean enableSuppression, boolean writableStackTrace) {
        super(message, sqlCause, enableSuppression, writableStackTrace);
        this.sqlException = sqlCause;
        this.sqlStatement = sqlStatement;
    }

    public SOSJDBCException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public SOSJDBCException(String message, Throwable cause, String sqlStatement, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.sqlStatement = sqlStatement;
    }

    public SOSJDBCException(String message, SQLException sqlCause, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.sqlException = sqlCause;
    }
    
    public SOSJDBCException(String message, SQLException sqlCause, Throwable cause, String sqlStatement, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.sqlException = sqlCause;
        this.sqlStatement = sqlStatement;
    }
    
    public SQLException getSqlException() {
        return sqlException;
    }
    
    public String getSqlStatement() {
        return sqlStatement;
    }
  
    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (sqlException != null && sqlException.getMessage() != null) {
            msg = String.format("%s: %d %s", msg, sqlException.getErrorCode(), sqlException.getMessage());
        }
        return msg;
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
