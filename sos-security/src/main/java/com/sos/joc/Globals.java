package com.sos.joc;

import com.sos.auth.rest.SOSShiroCurrentUsersList;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.model.SchedulerObjectFactory;

public class Globals {
    public static SOSShiroCurrentUsersList currentUsersList;
    public static SOSHibernateConnection sosHibernateConnection;
    public static SOSHibernateConnection sosSchedulerHibernateConnection;
    public static SchedulerObjectFactory schedulerObjectFactory;


}
