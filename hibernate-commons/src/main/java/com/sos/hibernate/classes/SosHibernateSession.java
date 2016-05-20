package com.sos.hibernate.classes;

import com.sos.hibernate.interfaces.IHibernateOptions;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/** @author Uwe Risse */
public class SosHibernateSession {

    private static final Logger LOGGER = Logger.getLogger(SosHibernateSession.class);
    private static Session session = null;
    private static SessionFactory sessionFactory = null;
    public static File configurationFile = null;

    protected SosHibernateSession() {
        // no Instances
    }

    public static Session getInstance(final File configurationFile) {
        if (session == null) {
            try {
                Configuration configuration = getConfiguration(getDefaultClassMapping());
                configuration.configure(configurationFile);
                configuration.setProperty("hibernate.jdbc.use_scrollable_resultset", "true");
                openSession(configuration);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            session.clear();
        }
        return session;
    }

    public static Session getInstance(final File configurationFile, int transactionIsolationLevel) {
        if (session == null) {
            try {
                Configuration configuration = getConfiguration(getDefaultClassMapping());
                configuration.configure(configurationFile);
                configuration.setProperty("hibernate.jdbc.use_scrollable_resultset", "true");
                if ("org.hibernate.dialect.MySQLInnoDBDialect".equals(configuration.getProperty("hibernate.dialect"))
                        || "org.hibernate.dialect.SQLServerDialect".equals(configuration.getProperty("hibernate.dialect"))) {
                    configuration.setProperty("hibernate.connection.isolation", String.valueOf(transactionIsolationLevel));
                } else {
                    configuration.setProperty("hibernate.connection.isolation", String.valueOf(Connection.TRANSACTION_READ_COMMITTED));
                }
                if (transactionIsolationLevel == Connection.TRANSACTION_READ_UNCOMMITTED) {
                    configuration.setProperty("hibernate.connection.autocommit", "true");
                }
                openSession(configuration);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            session.clear();
        }
        return session;
    }

    public static Session getInstance(IHibernateOptions options) {
        if (session == null) {
            try {
                Configuration configuration = getConfiguration(getDefaultClassMapping());
                configuration.setProperty("hibernate.connection.url", options.getHibernateConnectionUrl().getValue());
                configuration.setProperty("hibernate.connection.password", options.getHibernateConnectionPassword().getValue());
                configuration.setProperty("hibernate.connection.url", options.getHibernateConnectionUrl().getValue());
                configuration.setProperty("hibernate.connection.username", options.getHibernateConnectionUsername().getValue());
                configuration.setProperty("hibernate.dialect", options.getHibernateDialect().getValue());
                configuration.setProperty("hibernate.show_sql", options.getHibernateShowSql().getValue());
                configuration.setProperty("hibernate.connection.autocommit", options.getHibernateConnectionAutocommit().getValue());
                configuration.setProperty("hibernate.connection.isolation", options.getHibernateConnectionIsolation().getValue());
                configuration.setProperty("hibernate.format_sql", options.getHibernateFormatSql().getValue());
                configuration.setProperty("hibernate.jdbc.use_scrollable_resultset", options.getHibernateJdbcUseScrollableResultset().getValue());
                openSession(configuration);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            session.clear();
        }
        return session;
    }

    private static ClassList getDefaultClassMapping() {
        ClassList classList = new ClassList();
        classList.addClassIfExist("com.sos.dailyschedule.db.DailyScheduleDBItem");
        classList.addClassIfExist("com.sos.scheduler.history.db.SchedulerTaskHistoryDBItem");
        classList.addClassIfExist("com.sos.scheduler.history.db.SchedulerOrderStepHistoryDBItem");
        classList.addClassIfExist("com.sos.scheduler.history.db.SchedulerOrderHistoryDBItem");
        classList.addClassIfExist("com.sos.scheduler.db.SchedulerInstancesDBItem");
        classList.addClassIfExist("sos.jadehistory.db.JadeFilesDBItem");
        classList.addClassIfExist("sos.jadehistory.db.JadeFilesHistoryDBItem");
        classList.addClassIfExist("com.sos.eventing.db.SchedulerEventDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUserDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUserRightDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUserRoleDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUser2RoleDBItem");
        classList.addClassIfExist("com.sos.tools.logback.db.LoggingEventDBItem");
        classList.addClassIfExist("com.sos.tools.logback.db.LoggingEventExceptionDBItem");
        classList.addClassIfExist("com.sos.tools.logback.db.LoggingEventPropertyDBItem");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerOrderStepHistory");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerOrderHistory");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerVariables");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerHistory");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerMonSystemNotifications");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerMonResults");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerMonChecks");
        return classList;
    }

    private static Configuration getConfiguration(ClassList forClasses) {
        Configuration configuration = new Configuration();
        for (Class c : forClasses.getClasses()) {
            configuration.addAnnotatedClass(c);
        }
        return configuration;
    }

    private static void openSession(final Configuration configuration) {
        sessionFactory = configuration.buildSessionFactory();
        session = sessionFactory.openSession();
        session.doWork(new Work() {

            public void execute(Connection connection) throws SQLException {
                try {
                    connection.setTransactionIsolation(Integer.parseInt(configuration.getProperty("hibernate.connection.isolation")));
                } catch (NumberFormatException e) {
                    connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                }
                connection.setAutoCommit("true".equalsIgnoreCase(configuration.getProperty("hibernate.connection.autocommit")));
            }
        });
        session.setFlushMode(FlushMode.ALWAYS);
    }

    public static void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (session != null) {
            session.close();
        }
        session = null;
        sessionFactory = null;
    }

}