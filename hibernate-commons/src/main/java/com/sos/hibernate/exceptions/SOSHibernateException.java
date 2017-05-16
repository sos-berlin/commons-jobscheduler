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

    public SOSHibernateException(String msg, String stmt) {
        message = msg;
        statement = stmt;
    }

    @SuppressWarnings("deprecation")
    public SOSHibernateException(String msg, Query<?> query) {
        message = msg;
        statement = query.getQueryString();
    }

    public SOSHibernateException(String msg, Throwable cause) {
        message = msg;
        initCause(cause);
    }

    @SuppressWarnings("deprecation")
    public SOSHibernateException(Throwable cause, Query<?> query) {
        if (cause instanceof IllegalStateException) {
            if (cause.getCause() != null) {
                message = cause.getCause().getMessage();
            } else {
                message = cause.getMessage();
            }
        } else {
            message = String.format("%s %s", cause.getClass().getSimpleName(), cause.getMessage());
        }
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

    public SOSHibernateException(IllegalArgumentException cause, String stmt) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof QuerySyntaxException) {
                QuerySyntaxException je = (QuerySyntaxException) e;

                initCause(je);
                message = je.getMessage();// message contains hql as [hql statement]
                statement = je.getQueryString();
                // remove hql statement from the message
                if (message != null && statement != null) {
                    message = message.replace("[" + statement + "]", "");
                }

                return;
            }
            e = e.getCause();
        }
        initCause(cause);
        message = cause.getMessage();
        statement = stmt;
    }

    public SOSHibernateException(IllegalStateException cause, String stmt) {
        initCause(cause);
        message = cause.getMessage();
        statement = stmt;
    }

    @SuppressWarnings("deprecation")
    public SOSHibernateException(IllegalStateException cause, Query<?> query) {
        initCause(cause);
        message = cause.getMessage();
        statement = query.getQueryString();
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

    public void setMessage(String val) {
        message = val;
    }

    @Override
    public String toString() {
        if (statement != null) {
            return String.format("%s [%s]", super.toString(), statement);
        }
        return super.toString();
    }
}
