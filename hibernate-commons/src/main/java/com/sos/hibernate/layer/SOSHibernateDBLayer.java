package com.sos.hibernate.layer;

import java.io.File;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.sos.hibernate.classes.SosHibernateSession;
import com.sos.hibernate.interfaces.IHibernateOptions;

/** @author Uwe Risse */
public class SOSHibernateDBLayer {

    protected Session session = null;
    protected Transaction transaction = null;
    private static final Logger LOGGER = Logger.getLogger(SOSHibernateDBLayer.class);

    public SOSHibernateDBLayer() {
        //
    }

    private void initSessionEx(IHibernateOptions options) throws Exception {
        session = SosHibernateSession.getInstance(options);
        if (session == null) {
            String s = String.format("Could not initiate session for database using file %s", SosHibernateSession.configurationFile);
            throw new Exception(s);
        } else {
            session.setCacheMode(CacheMode.IGNORE);
        }
    }

    private void initSessionEx() throws Exception {
        session = SosHibernateSession.getInstance(SosHibernateSession.configurationFile);
        if (session == null) {
            String s = String.format("Could not initiate session for database using file %s", SosHibernateSession.configurationFile);
            throw new Exception(s);
        } else {
            session.setCacheMode(CacheMode.IGNORE);
        }
    }

    private void initSessionEx(int transactionIsolationLevel) throws Exception {
        session = SosHibernateSession.getInstance(SosHibernateSession.configurationFile, transactionIsolationLevel);
        if (session == null) {
            String s =
                    String.format("Could not initiate session for database using file %s and isolation_level %s",
                            SosHibernateSession.configurationFile, transactionIsolationLevel);
            throw new Exception(s);
        } else {
            session.setCacheMode(CacheMode.IGNORE);
        }
    }

    public void initSession() {
        try {
            initSessionEx();
        } catch (Exception e) {
            String s = String.format("Could not initiate session for database using file %s :", SosHibernateSession.configurationFile);
            LOGGER.error(s + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void initSession(IHibernateOptions options) {
        try {
            initSessionEx(options);
        } catch (Exception e) {
            String s = String.format("Could not initiate session for database using options", options.gethibernate_connection_driver_class());
            LOGGER.error(s + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void initSession(int transactionIsolationLevel) {
        try {
            initSessionEx(transactionIsolationLevel);
        } catch (Exception e) {
            String s =
                    String.format("Could not initiate session for database using file %s and isolation_level %s",
                            SosHibernateSession.configurationFile, transactionIsolationLevel);
            LOGGER.error(s + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public File getConfigurationFile() {
        return SosHibernateSession.configurationFile;
    }

    public void save(Object dBItem) {
        session.save(dBItem);
        session.flush();
    }

    public void update(Object dBItem) {
        session.update(dBItem);
        session.flush();
    }

    public void saveOrUpdate(Object dBItem) {
        session.saveOrUpdate(dBItem);
        session.flush();
    }

    public Session getSession() {
        if (session == null) {
            initSession();
        }
        return session;
    }

    public void delete(Object dBItem) {
        session.delete(dBItem);
        session.flush();
    }

    public Query createQuery(String hQuery) {
        return this.session.createQuery(hQuery);
    }

    public void beginTransaction() {
        initSession();
        transaction = session.beginTransaction();
    }

    public void beginTransaction(IHibernateOptions options) {
        initSession(options);
        transaction = session.beginTransaction();
    }

    public void beginTransaction(int transactionIsolationLevel) {
        initSession(transactionIsolationLevel);
        transaction = session.beginTransaction();
    }

    public void commit() {
        if (transaction != null) {
            session.flush();
            transaction.commit();
        }
    }

    public void closeSession() {
        if (session != null && session.isOpen()) {
            SosHibernateSession.close();
            session = null;
        }
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setSession(SOSHibernateDBLayer layer) {
        this.session = layer.getSession();
    }

    public void setConfigurationFile(File configurationFile) {
        SosHibernateSession.configurationFile = configurationFile;
    }

}
