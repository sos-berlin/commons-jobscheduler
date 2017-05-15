package com.sos.hibernate.exceptions;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import org.hibernate.JDBCException;
import org.hibernate.hql.internal.ast.QuerySyntaxException;
import org.hibernate.query.Query;

import com.sos.exception.SOSException;

public class SOSHibernateException extends SOSException {

    private static final long serialVersionUID = 1L;
    private String message = null;
    private SQLException sqlException = null;
    private String statement = null;

    public SOSHibernateException(String msg) {
        message = msg;
    }

    public SOSHibernateException(String msg, Throwable cause) {
        message = msg;
        initCause(cause);
    }

    @SuppressWarnings("deprecation")
    public SOSHibernateException(Query<?> query, Throwable cause) {
        message = String.format("%s %s", cause.getClass().getSimpleName(), cause.getMessage());
        statement = query.getQueryString();
        initCause(cause);
    }

    public SOSHibernateException(PersistenceException cause) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof JDBCException) {
                JDBCException je = (JDBCException) e;

                initCause(je);
                message = je.getMessage();
                sqlException = je.getSQLException();
                statement = je.getSQL();

                if (sqlException != null && sqlException.getMessage() != null) {
                    message = String.format("%s: %d %s", message, sqlException.getErrorCode(), sqlException.getMessage());
                }
                return;
            }
            e = e.getCause();
        }

        initCause(cause);
        message = String.format("%s %s", cause.getClass().getSimpleName(), cause.getMessage());
    }

    public SOSHibernateException(IllegalArgumentException cause) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof QuerySyntaxException) {
                QuerySyntaxException je = (QuerySyntaxException) e;

                initCause(je);
                message = je.getMessage();
                statement = je.getQueryString(); // hql statement is already in the message
                return;
            }
            e = e.getCause();
        }
        initCause(cause);
        message = cause.getMessage();
    }

    public SOSHibernateException(IllegalStateException cause) {
        initCause(cause);
        message = cause.getMessage();
    }

    public SOSHibernateException(SQLException cause, String sql) {
        initCause(cause);
        sqlException = cause;
        statement = sql;
        message = String.format("%d %s", sqlException.getErrorCode(), sqlException.getMessage());
    }

    public SQLException getSqlException() {
        return sqlException;
    }

    public String getStatement() {
        return statement;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
