package com.sos.hibernate.layer;

import java.io.File;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

import com.sos.hibernate.classes.ClassList;
import com.sos.hibernate.classes.SOSHibernateConnection;

/** @author Uwe Risse */
public class SOSHibernateDBLayer {

    private static final Logger LOGGER = Logger.getLogger(SOSHibernateDBLayer.class);
    protected SOSHibernateConnection connection = null;
    private String configurationFileName = null;
    protected StatelessSession statelessSession;
    protected Session session;

    public SOSHibernateDBLayer() {
    }

    public SOSHibernateDBLayer(String configFileName) {
        this.configurationFileName = configFileName;
        this.initConnection(this.getConfigurationFileName());
    }

    public void initConnection() {
        if (configurationFileName != null) {
            initConnection(configurationFileName);
        }
    }

    public void initConnection(String configFileName) {
        if (connection == null) {
            this.configurationFileName = configFileName;
            try {
                connection = new SOSHibernateConnection(configurationFileName);
                connection.addClassMapping(getDefaultClassMapping());

                connection.connect();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                LOGGER.error(String.format("Could not initiate hibernate connection for database using file %s", configurationFileName), e);
            }
        }
    }

    public void initConnection(SOSHibernateConnection connection) {
        this.connection = connection;
    }

    public String getConfigurationFileName() {
        return configurationFileName;
    }

    public File getConfigurationFile() {
        return new File(configurationFileName);
    }

    public void setConfigurationFileName(String configurationFileName) {
        this.configurationFileName = configurationFileName;
    }

    public SOSHibernateConnection getConnection() {
        return connection;
    }

    public void openSession() {
        if (this.connection.isUseOpenStatelessSession()) {
            this.statelessSession = this.connection.createStatelessSession();
        }else{
            this.session = (Session) this.connection.createSession();
        }
    }

    public Object getSession() {
        if (this.connection.isUseOpenStatelessSession()) {
            return this.statelessSession;
        }else{
            return this.session;
        }
    }
    
    public void closeSession() {
        if (statelessSession != null) {
            statelessSession.close();
        }
        if (session != null) {
            session.close();
        }
    }

    private static ClassList getDefaultClassMapping() {
        ClassList classList = new ClassList();
        classList.addClassIfExist("com.sos.jitl.dailyplan.db.DailyPlanDBItem");
        classList.addClassIfExist("com.sos.jitl.dailyplan.db.DailyPlanWithReportTriggerDBItem");
        classList.addClassIfExist("com.sos.jitl.dailyplan.db.DailyPlanWithReportExecutionDBItem");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerMonChecks");
        classList.addClassIfExist("com.sos.jitl.schedulerhistory.db.SchedulerTaskHistoryDBItem");
        classList.addClassIfExist("com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryDBItem");
        classList.addClassIfExist("com.sos.jitl.schedulerhistory.db.SchedulerOrderHistoryDBItem");
        classList.addClassIfExist("sos.jadehistory.db.JadeFilesDBItem");
        classList.addClassIfExist("sos.jadehistory.db.JadeFilesHistoryDBItem");
//        classList.addClassIfExist("com.sos.eventing.db.SchedulerEventDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUserDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUserPermissionDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUserRoleDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUser2RoleDBItem");
//        classList.addClassIfExist("com.sos.tools.logback.db.LoggingEventDBItem");
//        classList.addClassIfExist("com.sos.tools.logback.db.LoggingEventExceptionDBItem");
//        classList.addClassIfExist("com.sos.tools.logback.db.LoggingEventPropertyDBItem");
//        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerOrderStepHistory");
//        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerOrderHistory");
//        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerVariables");
//        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerHistory");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerMonSystemNotifications");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerMonResults");
        classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerMonChecks");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportTrigger");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportTriggerWithResult");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportTriggerResult");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportExecution");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportTriggerResult");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportExecutionDate");
        return classList;
    }

}
