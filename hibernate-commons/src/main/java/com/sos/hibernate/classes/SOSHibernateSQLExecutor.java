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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

public class SOSHibernateSQLExecutor implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSQLExecutor.class);

    private final SOSHibernateSession session;

    protected SOSHibernateSQLExecutor(SOSHibernateSession sess) {
        session = sess;
    }

    public void setDefaults() throws Exception {
        String method = getMethodName("setDefaults");

        Enum<SOSHibernateFactory.Dbms> dbms = session.getFactory().getDbms();
        LOGGER.debug(String.format("%s: dbms=%s", method, dbms));

        if (dbms.equals(SOSHibernateFactory.Dbms.MSSQL)) {
            String dateFormat = "set DATEFORMAT ymd";
            String language = "set LANGUAGE British";
            String lockTimeout = "set LOCK_TIMEOUT 3000";
            execute(dateFormat, language, lockTimeout);
        } else if (dbms.equals(SOSHibernateFactory.Dbms.MYSQL)) {
            execute("SET SESSION SQL_MODE='ANSI_QUOTES'");
        } else if (dbms.equals(SOSHibernateFactory.Dbms.ORACLE)) {
            String nlsNumericCharacters = "ALTER SESSION SET NLS_NUMERIC_CHARACTERS='.,'";
            String nlsDateFormat = "ALTER SESSION SET NLS_DATE_FORMAT='YYYY-MM-DD HH24:MI:SS'";
            String nlsSort = "ALTER SESSION SET NLS_SORT='BINARY'";
            executeBatch(nlsNumericCharacters, nlsDateFormat, nlsSort);
            executeUpdateCallableStatement("begin dbms_output.enable(10000); end;");
        } else if (dbms.equals(SOSHibernateFactory.Dbms.PGSQL)) {
            String lcNumeric = "SELECT set_config('lc_numeric', '', true)";
            String dateStyle = "SELECT set_config('datestyle', 'ISO, YMD', true)";
            String defaultTransactionIsolation = "SELECT set_config('default_transaction_isolation', 'repeatable read', true)";
            execute(lcNumeric, dateStyle, defaultTransactionIsolation);
        } else if (dbms.equals(SOSHibernateFactory.Dbms.SYBASE)) {
            String isolationLevel = "set TRANSACTION ISOLATION LEVEL READ COMMITTED";
            String chainedOn = "set CHAINED ON";
            String quotedIdentifier = "set QUOTED_IDENTIFIER ON";
            String lockTimeout = "set LOCK WAIT 3";
            String closeOnEndtran = "set CLOSE ON ENDTRAN ON";
            String datefirst = "set DATEFIRST 1";
            String dateFormat = "set DATEFORMAT 'ymd'";
            String language = "set LANGUAGE us_english";
            String textsize = "set TEXTSIZE 2048000";
            execute(isolationLevel, chainedOn, quotedIdentifier, lockTimeout, closeOnEndtran, datefirst, dateFormat, language, textsize);
        }
    }

    public int executeUpdateCallableStatement(String sql) throws Exception {
        String method = getMethodName("executeUpdateCallableStatement");
        int result = -1;
        Connection conn = session.getConnection();
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

    public void executeStatements(Path file) throws Exception {
        executeStatements(new String(Files.readAllBytes(file)));
    }

    public void executeStatements(String content) throws Exception {
        String method = getMethodName("executeStatements");

        Connection conn = session.getConnection();
        if (conn == null) {
            throw new Exception(String.format("%s: connection is null", method));
        }
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            List<String> commands = getStatements(content);
            for (int i = 0; i < commands.size(); i++) {
                String command = commands.get(i);
                if (isResultListQuery(command)) {
                    LOGGER.debug(String.format("%s: executeQuery: %s", method, command));
                    ResultSet rs = null;
                    try {
                        rs = stmt.executeQuery(command);
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        if (rs != null) {
                            rs.close();
                        }
                    }
                } else {
                    LOGGER.debug(String.format("%s: executeUpdate: %s", method, command));
                    stmt.executeUpdate(command);
                }
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public boolean execute(String... sqls) throws Exception {
        String method = getMethodName("execute");
        boolean result = false;
        Connection conn = session.getConnection();
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

    public int executeUpdate(String... sqls) throws Exception {
        String method = getMethodName("executeUpdate");
        int result = 0;
        Connection conn = session.getConnection();
        if (conn == null) {
            throw new Exception(String.format("%s: connection is null", method));
        }
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            for (String sql : sqls) {
                LOGGER.debug(String.format("%s: sql=%s", method, sql));
                result += stmt.executeUpdate(sql);
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

    public void executeQuery(String sql) throws Exception {
        String method = getMethodName("executeQuery");
        Connection conn = session.getConnection();
        if (conn == null) {
            throw new Exception(String.format("%s: connection is null", method));
        }
        Statement stmt = null;
        ResultSet rs = null;
        LOGGER.debug(String.format("%s: sql=%s", method, sql));
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (Exception e) {
            throw e;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /** ResultSet rs = session.getSQLExecutor().getResultSet("select * from ...");
     * 
     * Map<String, String> record = null;
     * 
     * while (!(record = session.getSQLExecutor().next(rs)).isEmpty()) {
     * 
     * LOGGER.info("record = " + result);
     * 
     * }
     * 
     * session.getSQLExecutor().close(rs); */
    public ResultSet getResultSet(String sql) throws Exception {
        String method = getMethodName("getResultSet");
        Connection conn = session.getConnection();
        if (conn == null) {
            throw new Exception(String.format("%s: connection is null", method));
        }
        Statement stmt = conn.createStatement();
        LOGGER.debug(String.format("%s: sql=%s", method, sql));
        return stmt.executeQuery(sql);
    }

    /** see executeQuery */
    public Map<String, String> next(ResultSet rs) throws Exception {
        Map<String, String> record = new LinkedHashMap<String, String>();
        if (rs != null && rs.next()) {
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            for (int i = 1; i <= count; i++) {
                String name = meta.getColumnName(i);
                String value = rs.getString(name);
                if (SOSString.isEmpty(value)) {
                    value = "";
                }
                record.put(name.toLowerCase(), value.trim());
            }
        }
        return record;
    }

    /** see executeQuery */
    public void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
            }
            try {
                rs.getStatement().close();
            } catch (SQLException e) {
            }
        }
    }

    public int[] executeBatch(String... sqls) throws Exception {
        String method = getMethodName("executeBatch");
        int[] result = null;
        Connection conn = session.getConnection();
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

    public int updateBlob(byte[] data, String tableName, String columnName, String condition) throws Exception {
        String method = getMethodName("updateBlob");

        if (data == null || data.length <= 0) {
            throw new Exception(String.format("%s: missing data", method));
        }
        int result = data.length;
        updateBlob(new ByteArrayInputStream(data), result, tableName, columnName, condition);
        return result;
    }

    public int updateBlob(Path path, String tableName, String columnName, String condition) throws Exception {
        String method = getMethodName("updateBlob");

        if (path == null) {
            throw new Exception(String.format("%s: path is null", method));
        }
        File file = path.toFile();
        int result = (int) file.length();
        if (!file.exists()) {
            throw new Exception(String.format("%s: file %s doesn't exist", method, file.getCanonicalPath()));
        }
        updateBlob(new FileInputStream(file), result, tableName, columnName, condition);
        return result;
    }

    public void updateBlob(InputStream inputStream, int dataLength, String tableName, String columnName, String condition) throws Exception {
        String method = getMethodName("updateBlob");

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
            Connection conn = session.getConnection();
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

    public int updateClob(String data, String tableName, String columnName, String condition) throws Exception {
        String method = getMethodName("updateClob");

        if (SOSString.isEmpty(data)) {
            throw new Exception(String.format("%s: missing data", method));
        }
        int result = data.length();
        updateClob(new java.io.StringReader(data), result, tableName, columnName, condition);
        return result;
    }

    public int updateClob(Path path, String tableName, String columnName, String condition) throws Exception {
        String method = getMethodName("updateClob");

        if (path == null) {
            throw new NullPointerException(String.format("%s: path is null.", method));
        }
        File file = path.toFile();
        int result = (int) file.length();
        if (!file.exists()) {
            throw new Exception(String.format("%s: file %s doesn't exist.", method, file.getCanonicalPath()));
        }
        updateClob(new FileReader(file), result, tableName, columnName, condition);
        return result;
    }

    public void updateClob(Reader reader, int dataLength, String tableName, String columnName, String condition) throws Exception {
        String method = getMethodName("updateClob");

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
            Connection conn = session.getConnection();
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

    public long getBlob(String sql, Path path) throws Exception {
        String method = getMethodName("getBlob");

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

            Connection conn = session.getConnection();
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

    public byte[] getBlob(String sql) throws Exception {
        String method = getMethodName("getBlob");

        Statement stmt = null;
        ResultSet rs = null;
        byte[] result = {};
        try {
            if (SOSString.isEmpty(sql)) {
                throw new Exception("missing sql");
            }

            Connection conn = session.getConnection();
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

    public String getClob(String sql) throws Exception {
        String method = getMethodName("getClob");

        Statement stmt = null;
        ResultSet rs = null;
        Reader in = null;
        StringBuilder result = new StringBuilder();
        try {
            if (SOSString.isEmpty(sql)) {
                throw new Exception("missing sql");
            }
            Connection conn = session.getConnection();
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

    public long getClob(String sql, Path path) throws Exception {
        String method = getMethodName("getClob");

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
            Connection conn = session.getConnection();
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

    public List<String> getStatements(Path file) throws Exception {
        return getStatements(new String(Files.readAllBytes(file)));
    }

    public List<String> getStatements(String content) throws Exception {
        SOSSQLCommandExtractor extractor = new SOSSQLCommandExtractor(session.getFactory().getDbms());
        return extractor.extractCommands(content);
    }

    private boolean isResultListQuery(String sql) {
        String patterns = "^select|^exec";
        Pattern p = Pattern.compile(patterns, Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(sql);
        return matcher.find();
    }

    private String getMethodName(String name) {
        String prefix = session.getIdentifier() == null ? "" : String.format("[%s] ", session.getIdentifier());
        return String.format("%s%s", prefix, name);
    }
}
