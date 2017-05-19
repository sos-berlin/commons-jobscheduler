package com.sos.hibernate.classes;

import java.lang.reflect.Method;

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
            Method[] ms = item.getClass().getDeclaredMethods();
            for (Method m : ms) {
                if (m.getName().startsWith("get")) {
                    Column c = m.getAnnotation(Column.class);
                    if (c != null) {
                        if (m.getAnnotation(Id.class) != null) {
                            try {
                                return m.invoke(item);
                            } catch (Throwable e) {
                                throw new SOSHibernateException(String.format("couldn't invoke @Id annotated method [%s.%s]", item.getClass()
                                        .getName(), m.getName()), e);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
