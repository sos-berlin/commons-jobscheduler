package com.sos.hibernate.layer;

import java.io.File;
import org.apache.log4j.Logger;
import com.sos.hibernate.classes.ClassList;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
  
/** @author Uwe Risse */
public class SOSHibernateDBLayer {

    private static final Logger LOGGER = Logger.getLogger(SOSHibernateDBLayer.class);
    protected SOSHibernateConnection connection = null;
    private SOSHibernateFactory sosHibernateFactory=null;
    private String configurationFileName = null;
  

    public SOSHibernateDBLayer() {
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
    
    public SOSHibernateConnection createStatefullConnection(String confFile) throws Exception{
        if (sosHibernateFactory == null) {
                sosHibernateFactory = new SOSHibernateFactory(confFile);
                sosHibernateFactory.addClassMapping(getDefaultClassMapping());
                sosHibernateFactory.open();
        }
        connection = new SOSHibernateConnection(sosHibernateFactory);
        connection.connect();
        return connection;
    }


    public SOSHibernateConnection createStatelessConnection(String confFile) throws Exception{
        if (sosHibernateFactory == null) {
                sosHibernateFactory = new SOSHibernateFactory(confFile);
                sosHibernateFactory.addClassMapping(getDefaultClassMapping());
                sosHibernateFactory.open();
        }
        connection = new SOSHibernateStatelessConnection(sosHibernateFactory);
        connection.connect();
        return connection;
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
        // classList.addClassIfExist("com.sos.eventing.db.SchedulerEventDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUserDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUserPermissionDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUserRoleDBItem");
        classList.addClassIfExist("com.sos.auth.shiro.db.SOSUser2RoleDBItem");
        // classList.addClassIfExist("com.sos.tools.logback.db.LoggingEventDBItem");
        // classList.addClassIfExist("com.sos.tools.logback.db.LoggingEventExceptionDBItem");
        // classList.addClassIfExist("com.sos.tools.logback.db.LoggingEventPropertyDBItem");
        // classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerOrderStepHistory");
        // classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerOrderHistory");
        // classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerVariables");
        // classList.addClassIfExist("com.sos.scheduler.notification.db.DBItemSchedulerHistory");
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
