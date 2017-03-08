package com.sos.hibernate.classes;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSHibernateBatchProcessor implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateBatchProcessor.class);
    private SOSHibernateSession session;
    private PreparedStatement preparedStatement;
    private String sqlStatement;
    private LinkedHashMap<String, Method> fieldsMetadata;
    private int countCurrentBatches;

    public SOSHibernateBatchProcessor(SOSHibernateSession sess) {
        session = sess;
        countCurrentBatches = 0;
    }

    public void createInsertBatch(Class<?> entity) throws Exception {
        String method = "createInsertBatch";
        LOGGER.debug(String.format("%s: entity = %s ", method, entity.getSimpleName()));
        if (session == null) {
            throw new Exception(String.format("%s: session is NULL", method));
        }
        if (session.getFactory().getDialect() == null) {
            throw new Exception(String.format("%s: dialect is NULL", method));
        }
        Table t = entity.getAnnotation(Table.class);
        if (t == null) {
            throw new Exception(String.format("%s: missing table annotation in entity %s", method, entity.getSimpleName()));
        }
        Method[] ms = entity.getDeclaredMethods();
        LinkedHashMap<String, Method> fieldsMap = new LinkedHashMap<String, Method>();
        String identifier = null;
        String sequenceNextValString = null;
        for (Method m : ms) {
            if (m.getName().startsWith("get")) {
                Column c = m.getAnnotation(Column.class);
                if (c != null) {
                    String fieldName = c.name().replaceAll("`", "");
                    Id id = m.getAnnotation(Id.class);
                    if (id == null) {
                        fieldsMap.put(fieldName, m);
                    } else {
                        GeneratedValue gv = m.getAnnotation(GeneratedValue.class);
                        if (gv == null) {
                            fieldsMap.put(fieldName, m);
                        } else {
                            try {
                                sequenceNextValString = session.getFactory().getDialect().getSelectSequenceNextValString(gv.generator());
                                identifier = fieldName;
                            } catch (Exception ex) {
                                // exception if not supported
                            }
                        }
                    }
                }
            }
        }
        StringBuilder sql = new StringBuilder("insert into " + t.name() + " (");
        StringBuilder sqlFields = new StringBuilder();
        if (identifier != null && sequenceNextValString != null) {
            sqlFields.append(session.getFactory().quoteFieldName(identifier));
        }
        for (Map.Entry<String, Method> entry : fieldsMap.entrySet()) {
            if (!sqlFields.toString().isEmpty()) {
                sqlFields.append(",");
            }
            sqlFields.append(session.getFactory().quoteFieldName(entry.getKey()));
        }
        sql.append(sqlFields);
        sql.append(") values (");
        sqlFields = new StringBuilder();
        if (identifier != null && sequenceNextValString != null) {
            sqlFields.append(sequenceNextValString);
        }
        for (int i = 0; i < fieldsMap.size(); i++) {
            if (!sqlFields.toString().isEmpty()) {
                sqlFields.append(",");
            }
            sqlFields.append("?");
        }
        sql.append(sqlFields);
        sql.append(")");
        sqlStatement = sql.toString();
        fieldsMetadata = fieldsMap;
        preparedStatement = session.getConnection().prepareStatement(sqlStatement);
    }

    public void addBatch(Object entity) throws Exception {
        String method = "addBatch";
        LOGGER.debug(String.format("%s: entity = %s ", method, entity.getClass().getSimpleName()));
        if (fieldsMetadata == null) {
            throw new Exception(String.format("%s: fieldsMetadata is NULL", method));
        }
        if (preparedStatement == null) {
            throw new Exception(String.format("%s: preparedStatement is NULL", method));
        }
        int index = 1;
        for (Map.Entry<String, Method> entry : fieldsMetadata.entrySet()) {
            Method m = entry.getValue();
            Object obj = m.invoke(entity);
            if (obj == null) {
                String rt = m.getReturnType().getSimpleName();
                if ("boolean".equalsIgnoreCase(rt)) {
                    preparedStatement.setNull(index, java.sql.Types.INTEGER);
                } else if ("long".equalsIgnoreCase(rt)) {
                    preparedStatement.setNull(index, java.sql.Types.INTEGER);
                } else if ("date".equalsIgnoreCase(rt)) {
                    preparedStatement.setNull(index, java.sql.Types.TIMESTAMP);
                } else {
                    preparedStatement.setString(index, null);
                }
            } else {
                if (obj instanceof Date) {
                    Date d = (Date) obj;
                    preparedStatement.setTimestamp(index, new Timestamp(d.getTime()));
                } else if (obj instanceof Long) {
                    preparedStatement.setLong(index, (Long) obj);
                } else if (obj instanceof Integer) {
                    preparedStatement.setInt(index, (Integer) obj);
                } else if (obj instanceof Boolean) {
                    preparedStatement.setInt(index, (Boolean) obj ? 1 : 0);
                } else {
                    preparedStatement.setString(index, obj.toString());
                }
            }
            index++;
        }
        preparedStatement.addBatch();
        countCurrentBatches++;
    }

    public int[] executeBatch() throws Exception {
        String method = "executeBatch";
        LOGGER.debug(String.format("%s", method));
        if (countCurrentBatches == 0) {
            return new int[0];
        }
        if (preparedStatement == null) {
            throw new Exception(String.format("%s: preparedStatement is NULL", method));
        }
        if (session == null) {
            throw new Exception(String.format("%s: connection is NULL", method));
        }
        try {
            int[] result = preparedStatement.executeBatch();
            session.getConnection().commit();
            return result;
        } catch (SQLException e) {
            try {
                session.getConnection().rollback();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            LOGGER.error(e.getMessage(), e);
            Throwable exx = e;
            if (e instanceof SQLException) {
                if (((SQLException) e.getNextException()) != null) {
                    exx = (SQLException) e.getNextException();
                }
            }
            throw new Exception(exx);
        } finally {
            countCurrentBatches = 0;
        }

    }

    public int getCountCurrentBatches() {
        return countCurrentBatches;
    }

    public static int getExecutedBatchSize(int[] arr) {
        int count = 0;
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                count += arr[i];
            }
        }
        return count;
    }

    public void close() {
        String method = "close";
        LOGGER.debug(String.format("%s", method));
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (Exception ex) {
                //
            }
        }
        dispose();
    }

    private void dispose() {
        countCurrentBatches = 0;
        preparedStatement = null;
        sqlStatement = null;
        fieldsMetadata = null;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public String getSqlStatement() {
        return sqlStatement;
    }

}