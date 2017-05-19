package com.sos.hibernate.classes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Id;

import org.hibernate.exception.LockAcquisitionException;

import com.sos.hibernate.exceptions.SOSHibernateException;

public class SOSHibernate {

    public static LockAcquisitionException findLockException(SOSHibernateException cause) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof LockAcquisitionException) {
                return (LockAcquisitionException) e;
            }
            e = e.getCause();
        }
        return null;
    }

    public static Object getId(Object item) throws SOSHibernateException {
        if (item != null) {
            Optional<Method> idAnnotatedMethod = Arrays.stream(item.getClass().getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Column.class)
                    && m.isAnnotationPresent(Id.class) && m.getName().startsWith("get")).findFirst();
            if (idAnnotatedMethod.isPresent()) {
                try {
                    return idAnnotatedMethod.get().invoke(item);
                } catch (Throwable e) {
                    throw new SOSHibernateException(String.format("couldn't invoke @Id annotated method [%s.%s]", item.getClass().getName(),
                            idAnnotatedMethod.get().getName()), e);
                }
            }
        }
        return null;
    }
}
