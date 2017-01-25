package com.sos.hibernate.classes;

public class SOSHibernateStatelessConnection extends SOSHibernateConnection {

	private static final long serialVersionUID = 1L;

	public SOSHibernateStatelessConnection(SOSHibernateFactory factory) {
        super(factory);
        this.setUseOpenStatelessSession(false);
    }

}
