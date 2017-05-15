package com.sos.hibernate.classes;

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
}
