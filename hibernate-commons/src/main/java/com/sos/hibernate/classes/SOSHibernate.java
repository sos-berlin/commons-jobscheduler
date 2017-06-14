package com.sos.hibernate.classes;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

import javax.persistence.Id;

import org.hibernate.exception.LockAcquisitionException;

import com.sos.hibernate.exceptions.SOSHibernateException;

public class SOSHibernate {

    public static final int LIMIT_IN_CLAUSE = 1000;

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
}
