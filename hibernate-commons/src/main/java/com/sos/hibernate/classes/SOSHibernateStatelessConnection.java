package com.sos.hibernate.classes;

public class SOSHibernateStatelessConnection extends SOSHibernateConnection {

    public SOSHibernateStatelessConnection(SOSHibernateFactory sosHibernateFactory) {
        super(sosHibernateFactory);
        this.setUseOpenStatelessSession(false);
    }

}
