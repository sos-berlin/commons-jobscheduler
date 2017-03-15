package com.sos.hibernate.layer;

import java.io.File;
import org.apache.log4j.Logger;
import com.sos.hibernate.classes.ClassList;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateFactory;
  
/** @author Uwe Risse */
public class SOSHibernateDBLayer {

    private static final Logger LOGGER = Logger.getLogger(SOSHibernateDBLayer.class);
    protected SOSHibernateSession sosHibernateSession = null;
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

    public SOSHibernateSession getSession() {
        return sosHibernateSession;
    }
    
    public SOSHibernateSession createStatefullConnection(String confFile) throws Exception{
        if (sosHibernateFactory == null) {
                sosHibernateFactory = new SOSHibernateFactory(confFile);
                sosHibernateFactory.addClassMapping(getDefaultClassMapping());
                sosHibernateFactory.build();
        }
        sosHibernateSession = sosHibernateFactory.openSession();
        return sosHibernateSession;
    }


    public SOSHibernateSession createStatelessConnection(String confFile) throws Exception{
        if (sosHibernateFactory == null) {
                sosHibernateFactory = new SOSHibernateFactory(confFile);
                sosHibernateFactory.addClassMapping(getDefaultClassMapping());
                sosHibernateFactory.build();
        }
        sosHibernateSession = sosHibernateFactory.openStatelessSession();
        return sosHibernateSession;
    }

    private static ClassList getDefaultClassMapping() {
        ClassList classList = new ClassList();
        classList.addClassIfExist("com.sos.jitl.dailyplan.db.DailyPlanDBItem");
        classList.addClassIfExist("com.sos.jitl.dailyplan.db.DailyPlanWithReportTriggerDBItem");
        classList.addClassIfExist("com.sos.jitl.dailyplan.db.DailyPlanWithReportExecutionDBItem");
        classList.addClassIfExist("com.sos.jitl.schedulerhistory.db.SchedulerTaskHistoryDBItem");
        classList.addClassIfExist("com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryDBItem");
        classList.addClassIfExist("com.sos.jitl.schedulerhistory.db.SchedulerOrderHistoryDBItem");
        classList.addClassIfExist("sos.jadehistory.db.JadeFilesDBItem");
        classList.addClassIfExist("sos.jadehistory.db.JadeFilesHistoryDBItem");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportTrigger");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportTriggerWithResult");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportTriggerResult");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportExecution");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportTriggerResult");
        classList.addClassIfExist("com.sos.jitl.reporting.db.DBItemReportExecutionDate");
        return classList;
    }

}
