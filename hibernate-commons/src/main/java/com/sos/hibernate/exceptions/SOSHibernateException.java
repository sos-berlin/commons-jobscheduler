package com.sos.hibernate.exceptions;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import org.hibernate.JDBCException;
import org.hibernate.StaleStateException;
import org.hibernate.hql.internal.ast.QuerySyntaxException;
import org.hibernate.query.Query;

import com.sos.exception.SOSException;
import com.sos.hibernate.classes.SOSHibernate;
import com.sos.hibernate.classes.SOSHibernateFactory;

public class SOSHibernateException extends SOSException {

    private static final long serialVersionUID = 1L;
    private static String STATEMENT_NOT_AVAILABLE = "n/a";
    private String message = null;
    private SQLException sqlException = null;
    private String statement = null;
    private String parameters = null;
    private Object dbItem = null;

    public SOSHibernateException(IllegalArgumentException cause, String stmt) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof QuerySyntaxException) {
                QuerySyntaxException je = (QuerySyntaxException) e;

                initCause(je);
                message = je.getMessage();// message can contains hql as [hql statement]
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

    public SOSHibernateException(IllegalStateException cause) {
        if (cause.getCause() == null) {
            initCause(cause);
            message = cause.getMessage();
        } else {
            initCause(cause.getCause());
            message = cause.getCause().getMessage();
        }
    }

    @SuppressWarnings("deprecation")
    public SOSHibernateException(IllegalStateException cause, Query<?> query) {
        if (cause.getCause() == null) {
            initCause(cause);
            message = cause.getMessage();
        } else {
            initCause(cause.getCause());
            message = cause.getCause().getMessage();
        }
        if (query != null) {
            statement = query.getQueryString();
            parameters = SOSHibernate.getQueryParametersAsString(query);
        }
    }

    public SOSHibernateException(IllegalStateException cause, String stmt) {
        if (cause.getCause() == null) {
            initCause(cause);
            message = cause.getMessage();
        } else {
            initCause(cause.getCause());
            message = cause.getCause().getMessage();
        }
        statement = stmt;
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
            } else if (e instanceof StaleStateException) {
                initCause(e);
                message = e.getMessage();
                return;
            }
            e = e.getCause();
        }

        initCause(cause);
        message = String.format("%s %s", cause.getClass().getSimpleName(), cause.getMessage());
    }

    public SOSHibernateException(PersistenceException cause, Query<?> query) {
        this(cause);
        handleStatement(query);
    }

    public SOSHibernateException(PersistenceException cause, String stmt) {
        this(cause);
        handleStatement(stmt);
    }

    public SOSHibernateException(SQLException cause) {
        initCause(cause);
        sqlException = cause;
        message = String.format("%d %s", sqlException.getErrorCode(), sqlException.getMessage());
    }

    public SOSHibernateException(SQLException cause, Query<?> query) {
        this(cause);
        handleStatement(query);
    }

    public SOSHibernateException(SQLException cause, String sql) {
        initCause(cause);
        sqlException = cause;
        statement = sql;
        message = String.format("%d %s", sqlException.getErrorCode(), sqlException.getMessage());
    }

    public SOSHibernateException(String msg) {
        message = msg;
    }

    @SuppressWarnings("deprecation")
    public SOSHibernateException(String msg, Query<?> query) {
        message = msg;
        if (query != null) {
            statement = query.getQueryString();
            parameters = SOSHibernate.getQueryParametersAsString(query);
        }
    }

    public SOSHibernateException(String msg, String stmt) {
        message = msg;
        statement = stmt;
    }

    public SOSHibernateException(String msg, Throwable cause) {
        message = msg;
        initCause(cause);
    }

    protected void setDbItem(Object val) {
        dbItem = val;
    }

    protected void setMessage(String val) {
        message = val;
    }

    protected void setStatement(String val) {
        statement = val;
    }

    public Object getDbItem() {
        return dbItem;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public SQLException getSQLException() {
        return sqlException;
    }

    public String getStatement() {
        return statement;
    }

    @Override
    public String toString() {
        String result = super.toString();
        if (statement != null) {
            result = String.format("%s [%s]", result, statement);
        }
        if (parameters != null) {
            result = String.format("%s[%s]", result, parameters);
        }
        if (dbItem != null) {
            result = String.format("%s [%s]", result, SOSHibernateFactory.toString(dbItem));
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    private void handleStatement(Query<?> query) {
        if (query != null) {
            handleStatement(query.getQueryString());
            parameters = SOSHibernate.getQueryParametersAsString(query);
        }
    }

    private void handleStatement(String stmt) {
        if (stmt != null && (statement == null || statement.equals(STATEMENT_NOT_AVAILABLE))) {
            statement = stmt;
            if (message != null) {
                // message can contains sql as [Query is:<new line> sql statement]
                int index = message.indexOf("Query is:");
                if (index > 0) {
                    message = message.substring(0, index).trim();
                }
            }
        }
    }
}
