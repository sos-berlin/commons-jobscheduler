package com.sos.hibernate.classes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Id;
import javax.persistence.Parameter;

import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.query.Query;

import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.exceptions.SOSHibernateInvalidSessionException;
import com.sos.hibernate.exceptions.SOSHibernateLockAcquisitionException;

import sos.util.SOSString;

public class SOSHibernate {

    public static final String HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT = "hibernate.connection.autocommit";
    public static final String HIBERNATE_PROPERTY_CONNECTION_URL = "hibernate.connection.url";
    public static final String HIBERNATE_PROPERTY_CONNECTION_USERNAME = "hibernate.connection.username";
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
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_FILE = "hibernate.sos.credential_store_file";
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_KEY_FILE = "hibernate.sos.credential_store_key_file";
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_PASSWORD = "hibernate.sos.credential_store_password";
    public static final String HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_ENTRY_PATH = "hibernate.sos.credential_store_entry_path";

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

    public static Exception findInvalidSessionException(Exception cause) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof SOSHibernateInvalidSessionException) {
                return (SOSHibernateInvalidSessionException) e;
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

    public static String getQueryParametersAsString(Query<?> query) {
        if (query == null) {
            return null;
        }
        try {
            Set<Parameter<?>> set = query.getParameters();
            if (set != null && set.size() > 0) {
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (Parameter<?> parameter : set) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(parameter.getName() + "=" + query.getParameterValue(parameter.getName()));
                    i++;
                }
                return sb.toString();
            }
        } catch (Throwable e) {
        }
        return null;
    }

    public static String toString(Object o) {
        if (o == null) {
            return null;
        }
        // exclude BLOB (byte[]) fields
        List<String> excludeFieldNames = Arrays.stream(o.getClass().getDeclaredFields()).filter(m -> m.getType().isAssignableFrom(byte[].class)).map(
                Field::getName).collect(Collectors.toList());
        return SOSString.toString(o, excludeFieldNames);
    }

    protected static String getLogIdentifier(String identifier) {
        return identifier == null ? "" : String.format("[%s]", identifier);
    }

    protected static String getMethodName(String logIdentifier, String name) {
        return String.format("%s[%s]", logIdentifier, name);
    }
}
