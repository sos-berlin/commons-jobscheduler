package com.sos.hibernate.classes;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

import javax.persistence.Id;

import org.hibernate.exception.LockAcquisitionException;

import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.exceptions.SOSHibernateLockAcquisitionException;

public class SOSHibernate {

    public static final String HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT = "hibernate.connection.autocommit";
    public static final String HIBERNATE_PROPERTY_CONNECTION_PASSWORD = "hibernate.connection.password";
    public static final String HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS = "hibernate.current_session_context_class";
    public static final String HIBERNATE_PROPERTY_ID_NEW_GENERATOR_MAPPINGS = "hibernate.id.new_generator_mappings";
    public static final String HIBERNATE_PROPERTY_JAVAX_PERSISTENCE_VALIDATION_MODE = "javax.persistence.validation.mode";
    public static final String HIBERNATE_PROPERTY_JDBC_FETCH_SIZE = "hibernate.jdbc.fetch_size";
    public static final String HIBERNATE_PROPERTY_TRANSACTION_ISOLATION = "hibernate.connection.isolation";
    public static final String HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET = "hibernate.jdbc.use_scrollable_resultset";
    // SOS configuration properties
    public static final String HIBERNATE_SOS_PROPERTY_MSSQL_LOCK_TIMEOUT = "hibernate.sos.mssql_lock_timeout";
    public static final String HIBERNATE_SOS_PROPERTY_SHOW_CONFIGURATION_PROPERTIES = "hibernate.sos.show_configuration_properties";
    public static final int LIMIT_IN_CLAUSE = 1000;

    public static Exception findLockException(Exception cause) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof SOSHibernateLockAcquisitionException) {
                return (SOSHibernateLockAcquisitionException) e;
            } else if (e instanceof LockAcquisitionException) {
                return (LockAcquisitionException) e;
            }
            e = e.getCause();
        }
        return null;
    }

    public static Object getId(Object item) throws SOSHibernateException {
        if (item != null) {
            Optional<Method> om = Arrays.stream(item.getClass().getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Id.class) && Modifier
                    .isPublic(m.getModifiers()) && !m.getReturnType().equals(void.class) && m.getName().startsWith("get")).findFirst();
            if (om.isPresent()) {
                Method m = om.get();
                try {
                    m.setAccessible(true);// make invoke faster
                    return m.invoke(item);
                } catch (Throwable e) {
                    throw new SOSHibernateException(String.format("couldn't invoke @Id annotated public getter method [%s.%s]", item.getClass()
                            .getName(), m.getName()), e);
                }
            }
        }
        return null;
    }

    protected static String getLogIdentifier(String identifier) {
        return identifier == null ? "" : String.format("[%s]", identifier);
    }

    protected static String getMethodName(String logIdentifier, String name) {
        return String.format("%s[%s]", logIdentifier, name);
    }
}
