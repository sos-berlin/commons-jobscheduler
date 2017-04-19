package com.sos.hibernate.classes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.internal.SessionImpl;
import org.hibernate.internal.StatelessSessionImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.DBSessionException;

import sos.util.SOSDate;
import sos.util.SOSString;

public class SOSHibernateSession implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSession.class);
    private final SOSHibernateFactory factory;
    private Object currentSession;
    private boolean isStatelessSession = false;
    private boolean isGetCurrentSession = false;
    private FlushMode defaultHibernateFlushMode = FlushMode.ALWAYS;
    private String identifier;
    private String openSessionMethodName;
    public static final int LIMIT_IN_CLAUSE = 1000;

    /** use factory.openSession() or factory.openStatelessSession(); */
    protected SOSHibernateSession(SOSHibernateFactory hibernateFactory) {
        this.factory = hibernateFactory;
    }

    public void setIdentifier(String val) {
        identifier = val;
    }

    protected void setIsStatelessSession(boolean val) {
        isStatelessSession = val;
    }

    protected void setIsGetCurrentSession(boolean val) {
        isGetCurrentSession = val;
    }

    public void setHibernateFlushMode(FlushMode flushMode) {
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            session.setHibernateFlushMode(flushMode);
        }
    }

    public void setCacheMode(CacheMode cacheMode) {
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            session.setCacheMode(cacheMode);
        }
    }

    public void setDefaults() throws Exception {
        String method = getMethodName("setDefaults");
        LOGGER.debug(String.format("%s: dbms=%s", method, factory.getDbms()));

        if (factory.getDbms().equals(SOSHibernateFactory.Dbms.MSSQL)) {
            String dateFormat = "set DATEFORMAT ymd";
            String language = "set LANGUAGE British";
            String lockTimeout = "set LOCK_TIMEOUT 3000";
            executeSQLStatement(dateFormat, language, lockTimeout);
        } else if (factory.getDbms().equals(SOSHibernateFactory.Dbms.MYSQL)) {
            executeSQLStatement("SET SESSION SQL_MODE='ANSI_QUOTES'");
        } else if (factory.getDbms().equals(SOSHibernateFactory.Dbms.ORACLE)) {
            String nlsNumericCharacters = "ALTER SESSION SET NLS_NUMERIC_CHARACTERS='.,'";
            String nlsDateFormat = "ALTER SESSION SET NLS_DATE_FORMAT='YYYY-MM-DD HH24:MI:SS'";
            String nlsSort = "ALTER SESSION SET NLS_SORT='BINARY'";
            executeSQLStatementBatch(nlsNumericCharacters, nlsDateFormat, nlsSort);
            executeUpdateSQLCallableStatement("begin dbms_output.enable(10000); end;");
        } else if (factory.getDbms().equals(SOSHibernateFactory.Dbms.PGSQL)) {
            String lcNumeric = "SELECT set_config('lc_numeric', '', true)";
            String dateStyle = "SELECT set_config('datestyle', 'ISO, YMD', true)";
            String defaultTransactionIsolation = "SELECT set_config('default_transaction_isolation', 'repeatable read', true)";
            executeSQLStatement(lcNumeric, dateStyle, defaultTransactionIsolation);
        } else if (factory.getDbms().equals(SOSHibernateFactory.Dbms.SYBASE)) {
            String isolationLevel = "set TRANSACTION ISOLATION LEVEL READ COMMITTED";
            String chainedOn = "set CHAINED ON";
            String quotedIdentifier = "set QUOTED_IDENTIFIER ON";
            String lockTimeout = "set LOCK WAIT 3";
            String closeOnEndtran = "set CLOSE ON ENDTRAN ON";
            String datefirst = "set DATEFIRST 1";
            String dateFormat = "set DATEFORMAT 'ymd'";
            String language = "set LANGUAGE us_english";
            String textsize = "set TEXTSIZE 2048000";
            executeSQLStatement(isolationLevel, chainedOn, quotedIdentifier, lockTimeout, closeOnEndtran, datefirst, dateFormat, language, textsize);
        }
    }

    public boolean isStatelessSession() {
        return isStatelessSession;
    }

    public boolean isGetCurrentSession() {
        return isGetCurrentSession;
    }

    public boolean isOpen() {
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            return session.isOpen();
        } else {
            StatelessSession session = ((StatelessSession) currentSession);
            return session.isOpen();
        }
    }

    public boolean isConnected() {
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            return session.isConnected();
        } else {
            StatelessSession session = ((StatelessSession) currentSession);
            return session.isConnected();
        }
    }

    public SOSHibernateFactory getFactory() {
        return factory;
    }

    public String getIdentifier() {
        return identifier;
    }

    public FlushMode getHibernateFlushMode() {
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            return session.getHibernateFlushMode();
        }
        return null;
    }
  
    public CacheMode getCacheMode() {
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            return session.getCacheMode();
        }
        return null;
    }

    public Object getCurrentSession() throws Exception {
        return currentSession;
    }

    public Connection getConnection() throws Exception {
        String method = "getConnection";
        if (currentSession instanceof Session) {
            SessionImpl sf = (SessionImpl) currentSession;
            try {
                return sf.connection();
            } catch (Exception e) {
                throw new Exception(String.format("%s: %s", method, e.toString()), e);
            }
        } else {
            StatelessSessionImpl sf = (StatelessSessionImpl) currentSession;
            try {
                return sf.connection();
            } catch (Exception e) {
                throw new Exception(String.format("%s: %s", method, e.toString()), e);
            }
        }
    }

    public static Throwable getException(Throwable ex) {
        if (ex instanceof SQLGrammarException) {
            SQLGrammarException sqlGrEx = (SQLGrammarException) ex;
            SQLException sqlEx = sqlGrEx.getSQLException();
            return new Exception(String.format("%s [exception: %s, sql: %s]", ex.getMessage(), sqlEx == null ? "" : sqlEx.getMessage(), sqlGrEx
                    .getSQL()), sqlEx);
        } else if (ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }

    public String getLastSequenceValue(String sequenceName) throws Exception {
        String stmt = factory.getSequenceLastValString(sequenceName);
        return stmt == null ? null : getNativeQuerySingleValue(stmt);
    }

    protected void openSession() throws Exception {
        String method = getMethodName("openSession");

        if (this.currentSession != null) {
            LOGGER.debug(String.format("%s: close currentSession", method));
            closeSession();
        }

        LOGGER.debug(String.format("%s: isStatelessSession = %s, isGetCurrentSession = %s", method, isStatelessSession, isGetCurrentSession));

        openSessionMethodName = "";
        if (isStatelessSession) {
            currentSession = factory.getSessionFactory().openStatelessSession();
            openSessionMethodName = "openStatelessSession";
        } else {
            Session session = null;
            if (isGetCurrentSession) {
                session = factory.getSessionFactory().getCurrentSession();
                openSessionMethodName = "getCurrentSession";
            } else {
                session = factory.getSessionFactory().openSession();
                openSessionMethodName = "openSession";
            }
            if (defaultHibernateFlushMode != null) {
                session.setHibernateFlushMode(defaultHibernateFlushMode);
            }
            currentSession = session;
        }
    }

    /** @deprecated
     * 
     *             use factory.openSession() or factory.openStatelessSession(); */
    @Deprecated
    public void connect() throws Exception {
        String method = getMethodName("connect");
        try {
            openSession();
            String connFile = (factory.getConfigFile().isPresent()) ? factory.getConfigFile().get().toAbsolutePath().toString()
                    : "without config file";
            int isolationLevel = getFactory().getTransactionIsolation();
            LOGGER.debug(String.format("%s: autocommit = %s, transaction isolation = %s, %s, %s", method, getFactory().getAutoCommit(),
                    SOSHibernateFactory.getTransactionIsolationName(isolationLevel), openSessionMethodName, connFile));
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    public void clearSession() throws Exception {
        String method = getMethodName("clearSession");
        LOGGER.debug(String.format("%s", method));
        if (currentSession == null) {
            throw new DBSessionException("session is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            session.clear();
        }
    }

    public void sessionDoWork(Work work) throws Exception {
        String method = getMethodName("sessionDoWork");
        LOGGER.debug(String.format("%s", method));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            session.doWork(work);
        } else {
            LOGGER.warn(String.format("%s: this method will be ignored for current openSessionMethodName : %s (%s)", method, openSessionMethodName,
                    currentSession.getClass().getSimpleName()));
        }
    }

    public void reopen() throws Exception {
        String method = getMethodName("reopen");
        try {
            LOGGER.info(String.format("%s: isStatelessSession = %s", method, isStatelessSession));
            closeSession();
            openSession();
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    /** @deprecated
     * 
     *             use close(); */
    @Deprecated
    public void disconnect() {
        close();
    }

    public void close() {
        String method = getMethodName("close");
        LOGGER.debug(String.format("%s", method));
        closeTransaction();
        closeSession();
    }

    public void beginTransaction() throws Exception {
        String method = getMethodName("beginTransaction");
        if (this.getFactory().getAutoCommit()) {
            LOGGER.debug(String.format("%s: skip (autoCommit is true)", method));
            return;
        }
        LOGGER.debug(String.format("%s", method));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.beginTransaction();
        } else {
            StatelessSession session = ((StatelessSession) currentSession);
            session.beginTransaction();
        }
    }

    public void commit() throws Exception {
        String method = getMethodName("commit");
        if (this.getFactory().getAutoCommit()) {
            LOGGER.debug(String.format("%s: skip (autoCommit is true)", method));
            return;
        }
        LOGGER.debug(String.format("%s", method));
        Transaction tr = getTransaction();
        if (tr == null) {
            throw new Exception(String.format("session transaction is NULL"));
        }
        if (currentSession instanceof Session) {
            ((Session) currentSession).flush();
        }
        tr.commit();
    }

    public void rollback() throws Exception {
        String method = getMethodName("rollback");
        if (this.getFactory().getAutoCommit()) {
            LOGGER.debug(String.format("%s: skip (autoCommit is true)", method));
            return;
        }
        LOGGER.debug(String.format("%s", method));
        Transaction tr = getTransaction();
        if (tr == null) {
            throw new Exception(String.format("session transaction is NULL"));
        }
        tr.rollback();
    }

    public Transaction getTransaction() throws Exception {
        Transaction tr = null;
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        if (currentSession instanceof Session) {
            Session s = ((Session) currentSession);
            tr = s.getTransaction();
        } else {
            StatelessSession s = ((StatelessSession) currentSession);
            tr = s.getTransaction();
        }
        return tr;
    }

    public void save(Object item) throws Exception {
        String method = getMethodName("save");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new DBSessionException("session is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.save(item);
            session.flush();
        } else {
            StatelessSession session = ((StatelessSession) currentSession);
            session.insert(item);
        }
    }

    public void update(Object item) throws Exception {
        String method = getMethodName("update");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new DBSessionException("session is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.update(item);
            session.flush();
        } else {
            StatelessSession session = ((StatelessSession) currentSession);
            session.update(item);
        }
    }

    public Object saveOrUpdate(Object item) throws Exception {
        String method = getMethodName("saveOrUpdate");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.saveOrUpdate(item);
            session.flush();
        } else {
            StatelessSession session = ((StatelessSession) currentSession);
            /*
             * The following error will always be logged in the try segment, if the item id field is null: SQL Error: -1, SQLState: 07004 Parameter at position
             * 9 is not set HHH000010: On release of batch it still contained JDBC statements in a stateless session it is better to check if the item is a new
             * entry and then call save() or an existing item and then call update() there is no need to create an error to switch the statement afterwards
             */
            try {
                session.update(item);
            } catch (Exception e) {
                session.insert(item);
            }
        }
        return item;
    }

    public void delete(Object item) throws Exception {
        String method = getMethodName("delete");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.delete(item);
            session.flush();
        } else {
            StatelessSession session = ((StatelessSession) currentSession);
            session.delete(item);
        }
    }

    public void refresh(Object object) {
        refresh(null, object);
    }

    public void refresh(String entityName, Object object) {
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            if (entityName == null) {
                session.refresh(object);
            } else {
                session.refresh(entityName, object);
            }
        } else {
            StatelessSession session = ((StatelessSession) currentSession);
            if (entityName == null) {
                session.refresh(object);
            } else {
                session.refresh(entityName, object);
            }

        }
    }

    public Object get(Class<?> entityClass, Serializable id) throws Exception {
        if (currentSession instanceof Session) {
            return ((Session) currentSession).get(entityClass, id);
        } else {
            return ((StatelessSession) currentSession).get(entityClass, id);
        }
    }

    public int executeUpdateSQLCallableStatement(String sql) throws Exception {
        String method = getMethodName("executeUpdateSQLCallableStatement");
        int result = -1;
        Connection conn = getConnection();
        if (conn == null) {
            throw new Exception(String.format("%s: connection is null", method));
        }
        LOGGER.debug(String.format("%s: sqlStmt=%s", method, sql));
        CallableStatement stmt = null;
        try {
            stmt = conn.prepareCall(sql);
            result = stmt.executeUpdate();
        } catch (Throwable e) {
            throw e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable e) {
                }
            }
        }
        return result;
    }

    public boolean executeSQLStatement(String... sqls) throws Exception {
        String method = getMethodName("executeSQLStatement");
        boolean result = false;
        Connection conn = getConnection();
        if (conn == null) {
            throw new Exception(String.format("%s: connection is null", method));
        }
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            for (String sql : sqls) {
                LOGGER.debug(String.format("%s: sql=%s", method, sql));
                result = stmt.execute(sql);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable e) {
                }
            }
        }
        return result;
    }

    public int[] executeSQLStatementBatch(String... sqls) throws Exception {
        String method = getMethodName("executeSQLStatementBatch");
        int[] result = null;
        Connection conn = getConnection();
        if (conn == null) {
            throw new Exception(String.format("%s: connection is null", method));
        }
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            for (String sql : sqls) {
                LOGGER.debug(String.format("%s: addBatch sql=%s", method, sql));
                stmt.addBatch(sql);
            }
            result = stmt.executeBatch();
        } catch (Throwable e) {
            throw e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable e) {
                }
            }
        }
        return result;
    }

    public int executeSQLStatementUpdateBlob(byte[] data, String tableName, String columnName, String condition) throws Exception {
        String method = getMethodName("executeSQLStatementUpdateBlob");

        if (data == null || data.length <= 0) {
            throw new Exception(String.format("%s: missing data", method));
        }
        int result = data.length;
        executeSQLStatementUpdateBlob(new ByteArrayInputStream(data), result, tableName, columnName, condition);
        return result;
    }

    public int executeSQLStatementUpdateBlob(Path path, String tableName, String columnName, String condition) throws Exception {
        String method = getMethodName("executeSQLStatementUpdateBlob");

        if (path == null) {
            throw new Exception(String.format("%s: path is null", method));
        }
        File file = path.toFile();
        int result = (int) file.length();
        if (!file.exists()) {
            throw new Exception(String.format("%s: file %s doesn't exist", method, file.getCanonicalPath()));
        }
        executeSQLStatementUpdateBlob(new FileInputStream(file), result, tableName, columnName, condition);
        return result;
    }

    public void executeSQLStatementUpdateBlob(InputStream inputStream, int dataLength, String tableName, String columnName, String condition)
            throws Exception {
        String method = getMethodName("executeSQLStatementUpdateBlob");

        PreparedStatement pstmt = null;
        try {
            if (SOSString.isEmpty(tableName)) {
                throw new Exception("missing tableName");
            }
            if (SOSString.isEmpty(columnName)) {
                throw new Exception("missing columnName");
            }
            if (inputStream == null) {
                throw new Exception("input stream is null");
            }
            StringBuilder query = new StringBuilder();
            query.append("UPDATE ");
            query.append(tableName);
            query.append(" SET ").append(columnName).append(" = ? ");
            if (condition != null) {
                String where = condition.trim();
                if (!SOSString.isEmpty(where)) {
                    if (where.toUpperCase().startsWith("WHERE")) {
                        query.append(" ").append(where);
                    } else {
                        query.append(" WHERE ").append(where);
                    }
                }
            }
            Connection conn = getConnection();
            if (conn == null) {
                throw new Exception("connection is null");
            }
            pstmt = conn.prepareStatement(query.toString());
            pstmt.setBinaryStream(1, inputStream, dataLength);
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    //
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    //
                }
            }
        }
    }

    public int executeSQLStatementUpdateClob(String data, String tableName, String columnName, String condition) throws Exception {
        String method = getMethodName("executeSQLStatementUpdateClob");

        if (SOSString.isEmpty(data)) {
            throw new Exception(String.format("%s: missing data", method));
        }
        int result = data.length();
        executeSQLStatementUpdateClob(new java.io.StringReader(data), result, tableName, columnName, condition);
        return result;
    }

    public int executeSQLStatementUpdateClob(Path path, String tableName, String columnName, String condition) throws Exception {
        String method = getMethodName("executeSQLStatementUpdateClob");

        if (path == null) {
            throw new NullPointerException(String.format("%s: path is null.", method));
        }
        File file = path.toFile();
        int result = (int) file.length();
        if (!file.exists()) {
            throw new Exception(String.format("%s: file %s doesn't exist.", method, file.getCanonicalPath()));
        }
        executeSQLStatementUpdateClob(new FileReader(file), result, tableName, columnName, condition);
        return result;
    }

    public void executeSQLStatementUpdateClob(Reader reader, int dataLength, String tableName, String columnName, String condition) throws Exception {
        String method = getMethodName("executeSQLStatementUpdateClob");

        PreparedStatement pstmt = null;
        try {
            if (SOSString.isEmpty(tableName)) {
                throw new Exception("missing tableName.");
            }
            if (SOSString.isEmpty(columnName)) {
                throw new NullPointerException("missing columnName.");
            }
            if (reader == null) {
                throw new Exception("reader is null.");
            }
            StringBuilder query = new StringBuilder();
            query.append("UPDATE ");
            query.append(tableName);
            query.append(" SET ").append(columnName).append(" = ? ");
            if (condition != null) {
                String where = condition.trim();
                if (!SOSString.isEmpty(where)) {
                    if (where.toUpperCase().startsWith("WHERE")) {
                        query.append(" ").append(where);
                    } else {
                        query.append(" WHERE ").append(where);
                    }
                }
            }
            Connection conn = getConnection();
            if (conn == null) {
                throw new Exception("connection is null");
            }
            pstmt = conn.prepareStatement(query.toString());
            pstmt.setCharacterStream(1, reader, dataLength);
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    //
                }
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    public long getSQLStatementBlob(String sql, Path path) throws Exception {
        String method = getMethodName("getSQLStatementBlob");

        Statement stmt = null;
        ResultSet rs = null;
        InputStream in = null;
        FileOutputStream out = null;

        long result = 0;
        try {
            if (SOSString.isEmpty(sql)) {
                throw new Exception("missing sql");
            }
            if (path == null) {
                throw new Exception("path is null");
            }

            Connection conn = getConnection();
            if (conn == null) {
                throw new Exception("connection is null");
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            int len = 0;
            if (rs.next()) {
                in = rs.getBinaryStream(1);
                if (in == null) {
                    return result;
                }
                byte[] buff = new byte[1024];
                if ((len = in.read(buff)) > 0) {
                    out = new FileOutputStream(path.toFile());
                    out.write(buff, 0, len);
                    result += len;
                } else {
                    return result;
                }
                while (0 < (len = in.read(buff))) {
                    out.write(buff, 0, len);
                    result += len;
                }
            }
        } catch (Exception e) {
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    //
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    //
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    //
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    //
                }
            }
        }
        return result;
    }

    public byte[] getSQLStatementBlob(String sql) throws Exception {
        String method = getMethodName("getSQLStatementBlob");

        Statement stmt = null;
        ResultSet rs = null;
        byte[] result = {};
        try {
            if (SOSString.isEmpty(sql)) {
                throw new Exception("missing sql");
            }

            Connection conn = getConnection();
            if (conn == null) {
                throw new Exception("connection is null");
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                result = rs.getBytes(1);
            }
            if (result == null) {
                return result;
            }
        } catch (Exception e) {
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    //
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    //
                }
            }

        }
        return result;
    }

    public String getSQLStatementClob(String sql) throws Exception {
        String method = getMethodName("getSQLStatementClob");

        Statement stmt = null;
        ResultSet rs = null;
        Reader in = null;
        StringBuilder result = new StringBuilder();
        try {
            if (SOSString.isEmpty(sql)) {
                throw new Exception("missing sql");
            }
            Connection conn = getConnection();
            if (conn == null) {
                throw new Exception("connection is null");
            }
            stmt = conn.createStatement();
            try {
                rs = stmt.executeQuery(sql);
            } catch (Exception e) {
                throw new Exception("exception on executeQuery: " + e.toString());
            }

            int bytesRead;
            if (rs.next()) {
                in = rs.getCharacterStream(1);
                if (in == null) {
                    return "";
                }
                if ((bytesRead = in.read()) != -1) {
                    result.append((char) bytesRead);
                } else {
                    return "";
                }
                while ((bytesRead = in.read()) != -1) {
                    result.append((char) bytesRead);
                }
            }
        } catch (Exception e) {
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    //
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    //
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    //
                }
            }
        }
        return result.toString();
    }

    public long getSQLStatementClob(String sql, Path path) throws Exception {
        String method = getMethodName("getSQLStatementClob");

        Statement stmt = null;
        ResultSet rs = null;
        Reader in = null;
        FileOutputStream out = null;
        long result = 0;
        try {
            if (SOSString.isEmpty(sql)) {
                throw new Exception("missing sql");
            }
            if (path == null) {
                throw new Exception("path is null");
            }
            Connection conn = getConnection();
            if (conn == null) {
                throw new Exception("connection is null");
            }

            stmt = conn.createStatement();
            try {
                rs = stmt.executeQuery(sql);
            } catch (Exception e) {
                throw new Exception("exception on executeQuery: " + e.toString());
            }

            int bytesRead = 0;
            if (rs.next()) {
                in = rs.getCharacterStream(1);
                if (in == null) {
                    return result;
                }
                if ((bytesRead = in.read()) != -1) {
                    out = new FileOutputStream(path.toFile());
                    out.write(bytesRead);
                    result++;
                } else {
                    return result;
                }
                while ((bytesRead = in.read()) != -1) {
                    out.write(bytesRead);
                    result++;
                }
            }
        } catch (Exception e) {
            throw new Exception(String.format("%s: %s", method, e.toString()), e);
        } finally {
            try {
                out.flush();
            } catch (Exception e) {
                //
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    //
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    //
                }
            }

            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    //
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    //
                }
            }

        }
        return result;
    }

    public List<String> getNativeQueries(Path file) throws Exception {
        return getNativeQueries(new String(Files.readAllBytes(file)));
    }

    public List<String> getNativeQueries(String content) throws Exception {
        SOSSqlCommandExtractor extractor = new SOSSqlCommandExtractor(this.factory.getDbms());
        return extractor.extractCommands(content);
    }

    public void executeNativeQueries(Path file) throws Exception {
        executeNativeQueries(new String(Files.readAllBytes(file)));
    }

    public void executeNativeQueries(String content) throws Exception {
        try {
            beginTransaction();

            List<String> commands = getNativeQueries(content);
            for (int i = 0; i < commands.size(); i++) {
                NativeQuery<?> q = createNativeQuery(commands.get(i));
                if (isResultListQuery(commands.get(i))) {
                    q.getResultList();
                } else {
                    q.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            rollback();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Query<T> createQuery(String hql) throws Exception {
        String method = getMethodName("createQuery");
        LOGGER.debug(String.format("%s: hql = %s", method, hql));
        if (currentSession == null) {
            throw new DBSessionException("Session is NULL");
        }
        Query<T> q = null;
        if (currentSession instanceof Session) {
            q = ((Session) currentSession).createQuery(hql);
        } else {
            q = ((StatelessSession) currentSession).createQuery(hql);
        }
        return q;
    }

    /** @deprecated method for compatibility with the 1.11.0 an 1.11.1 versions use createNativeQuery */
    @Deprecated
    public SQLQuery<?> createSQLQuery(String sql) throws Exception {
        return createSQLQuery(sql, null);
    }

    /** @deprecated method for compatibility with the 1.11.0 an 1.11.1 versions use createNativeQuery */
    @Deprecated
    public SQLQuery<?> createSQLQuery(String sql, Class<?> entityClass) throws Exception {
        String method = getMethodName("createSQLQuery");
        LOGGER.debug(String.format("%s: sql=%s", method, sql));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        SQLQuery<?> q = null;
        if (currentSession instanceof Session) {
            q = ((Session) currentSession).createSQLQuery(sql);
        } else {
            q = ((StatelessSession) currentSession).createSQLQuery(sql);
        }
        if (q != null && entityClass != null) {
            q.addEntity(entityClass);
        }
        return q;
    }

    public <T> NativeQuery<T> createNativeQuery(String sql) throws Exception {
        return createNativeQuery(sql, null);
    }

    @SuppressWarnings("unchecked")
    public <T> NativeQuery<T> createNativeQuery(String sql, Class<T> entityClass) throws Exception {
        String method = getMethodName("createNativeQuery");
        LOGGER.debug(String.format("%s: sql=%s", method, sql));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        NativeQuery<T> q = null;
        if (currentSession instanceof Session) {
            if (entityClass == null) {
                q = ((Session) currentSession).createNativeQuery(sql);
            } else {
                q = ((Session) currentSession).createNativeQuery(sql, entityClass);
            }
        } else {
            if (entityClass == null) {
                q = ((StatelessSession) currentSession).createNativeQuery(sql);
            } else {
                q = ((StatelessSession) currentSession).createNativeQuery(sql, entityClass);
            }
        }
        return q;
    }

    public String getSingleValue(String hql) throws Exception {
        return getSingleValue(createQuery(hql));
    }

    public String getNativeQuerySingleValue(String sql) throws Exception {
        return getSingleValue(createNativeQuery(sql));
    }

    /** return the first possible value or null
     * 
     * difference to Query.getSingleResult - not throw NoResultException, return single value as string */
    public <T> String getSingleValue(Query<T> query) throws Exception {
        String result = null;

        List<T> results = query.getResultList();
        if (results != null && !results.isEmpty()) {
            if (results.get(0) instanceof Object[]) {
                Object[] obj = (Object[]) results.get(0);
                result = obj[0] + "";
            } else {
                result = results.get(0) + "";
            }
        }
        return result;
    }

    public <T> T getSingleResult(String hql) throws Exception {
        return getSingleResult(createQuery(hql));
    }

    /** return the first possible result or null
     * 
     * difference to Query.getSingleResult - not throw NoResultException */
    public <T> T getSingleResult(Query<T> query) throws Exception {
        T result = null;

        List<T> results = query.getResultList();
        if (results != null && !results.isEmpty()) {
            result = results.get(0);
        }
        return result;
    }

    public <T> Map<String, String> getNativeQuerySingleResult(String sql) throws Exception {
        return getNativeQuerySingleResult(sql, null);
    }

    public <T> Map<String, String> getNativeQuerySingleResult(String sql, String dateTimeFormat) throws Exception {
        return getSingleResult(createNativeQuery(sql), dateTimeFormat);
    }

    public <T> Map<String, String> getSingleResult(NativeQuery<T> query) throws Exception {
        return getSingleResult(query, null);
    }

    /** return a single row represented by Map<String,String>
     * 
     * Map - see getResultList */
    public <T> Map<String, String> getSingleResult(NativeQuery<T> query, String dateTimeFormat) throws Exception {
        Map<String, String> result = null;
        List<Map<String, String>> resultList = getResultList(query, dateTimeFormat);
        if (resultList != null && !resultList.isEmpty()) {
            result = new HashMap<String, String>();
            Map<String, String> map = resultList.get(0);
            for (String key : map.keySet()) {
                result.put(key, map.get(key));
            }
        }
        return result;
    }

    public <T> List<Map<String, String>> getNativeQueryResultList(String sql) throws Exception {
        return getNativeQueryResultList(sql, null);
    }

    public <T> List<Map<String, String>> getNativeQueryResultList(String sql, String dateTimeFormat) throws Exception {
        return getResultList(createNativeQuery(sql), dateTimeFormat);
    }

    public <T> List<Map<String, String>> getResultList(NativeQuery<T> query) throws Exception {
        return getResultList(query, null);
    }

    /** return a list of rows represented by Map<String,String>:
     * 
     * Map key - column name (lower case), Map value - value as string
     * 
     * 
     * setResultTransformer is deprecated (see below), but currently without alternative
     * 
     * excerpt from Query.setResultTransformer comment:
     * 
     * deprecated (since 5.2), todo develop a new approach to result transformers */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public <T> List<Map<String, String>> getResultList(NativeQuery<T> query, String dateTimeFormat) throws Exception {
        query.setResultTransformer(new ResultTransformer() {

            private static final long serialVersionUID = 1L;

            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                Map<String, String> result = new HashMap<String, String>(tuple.length);
                for (int i = 0; i < tuple.length; i++) {
                    String alias = aliases[i];
                    if (alias != null) {
                        Object origValue = tuple[i];
                        String value = "";
                        if (origValue != null) {
                            value = origValue + "";
                            if (origValue instanceof java.sql.Timestamp && dateTimeFormat != null) {
                                try {
                                    value = SOSDate.getDateTimeAsString(value, dateTimeFormat);
                                } catch (Exception e) {
                                }
                            }
                        }
                        result.put(alias.toLowerCase(), value);
                    }
                }
                return result;
            }

            @SuppressWarnings("rawtypes")
            @Override
            public List<?> transformList(List collection) {
                return collection;
            }
        });
        return (List<Map<String, String>>) query.getResultList();
    }

    @Deprecated
    public Criteria createCriteria(Class<?> cl, String alias) throws Exception {
        String method = getMethodName("createCriteria");
        LOGGER.debug(String.format("%s: class = %s", method, cl.getSimpleName()));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        Criteria cr = null;
        if (currentSession instanceof Session) {
            cr = ((Session) currentSession).createCriteria(cl, alias);
        } else {
            cr = ((StatelessSession) currentSession).createCriteria(cl, alias);
        }
        return cr;
    }

    @Deprecated
    public Criteria createCriteria(Class<?> cl) throws Exception {
        return createCriteria(cl, (String) null);
    }

    @Deprecated
    public Criteria createCriteria(Class<?> cl, String[] selectProperties) throws Exception {
        return createCriteria(cl, selectProperties, null);
    }

    @Deprecated
    public Criteria createCriteria(Class<?> cl, String[] selectProperties, ResultTransformer transformer) throws Exception {
        Criteria cr = createCriteria(cl);
        if (cr == null) {
            throw new Exception("Criteria is NULL");
        }
        if (selectProperties != null) {
            ProjectionList pl = Projections.projectionList();
            for (String property : selectProperties) {
                pl.add(Projections.property(property), property);
            }
            cr.setProjection(pl);
        }
        if (transformer != null) {
            cr.setResultTransformer(transformer);
        }
        return cr;
    }

    @Deprecated
    public Criteria createSingleListTransform2BeanCriteria(Class<?> cl, String selectProperty) throws Exception {
        return createSingleListCriteria(cl, selectProperty, Transformers.aliasToBean(cl));
    }

    @Deprecated
    public Criteria createSingleListCriteria(Class<?> cl, String selectProperty) throws Exception {
        return createSingleListCriteria(cl, selectProperty, null);
    }

    @Deprecated
    public Criteria createSingleListCriteria(Class<?> cl, String selectProperty, ResultTransformer transformer) throws Exception {
        return createCriteria(cl, new String[] { selectProperty }, transformer);
    }

    @Deprecated
    public Criteria createTransform2BeanCriteria(Class<?> cl) throws Exception {
        return createCriteria(cl, null, null);
    }

    @Deprecated
    public Criteria createTransform2BeanCriteria(Class<?> cl, String[] selectProperties) throws Exception {
        return createCriteria(cl, selectProperties, Transformers.aliasToBean(cl));
    }

    public static Criterion createInCriterion(String propertyName, List<?> list) {
        Criterion criterion = null;
        int size = list.size();

        for (int i = 0; i < size; i += LIMIT_IN_CLAUSE) {
            List<?> subList;
            if (size > i + LIMIT_IN_CLAUSE) {
                subList = list.subList(i, (i + LIMIT_IN_CLAUSE));
            } else {
                subList = list.subList(i, size);
            }
            if (criterion != null) {
                criterion = Restrictions.or(criterion, Restrictions.in(propertyName, subList));
            } else {
                criterion = Restrictions.in(propertyName, subList);
            }
        }
        return criterion;
    }

    private String getMethodName(String name) {
        String prefix = identifier == null ? "" : String.format("[%s] ", identifier);
        return String.format("%s%s", prefix, name);
    }

    private void closeTransaction() {
        String method = getMethodName("closeTransaction");
        try {
            if (currentSession != null) {
                Transaction tr = getTransaction();
                if (tr != null) {
                    LOGGER.debug(String.format("%s: rollback", method));
                    tr.rollback();
                } else {
                    LOGGER.debug(String.format("%s: skip rollback (transaction is null)", method));
                }
            }
        } catch (Exception ex) {
            //
        }

    }

    private void closeSession() {
        String method = getMethodName("closeSession");
        LOGGER.debug(String.format("%s", method));
        try {
            if (currentSession != null) {
                if (currentSession instanceof Session) {
                    Session session = (Session) currentSession;
                    if (session.isOpen()) {
                        session.close();
                    }
                } else {
                    StatelessSession s = (StatelessSession) currentSession;
                    s.close();
                }
            }
        } catch (Throwable e) {
        }
        currentSession = null;
        openSessionMethodName = null;
    }

    private boolean isResultListQuery(String sql) {
        String patterns = "^select|^exec";
        Pattern p = Pattern.compile(patterns, Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(sql);
        return matcher.find();
    }
}