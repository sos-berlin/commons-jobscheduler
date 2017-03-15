package com.sos.hibernate.classes;

/** @deprecated
 * 
 *             use factory.openStatelessSession(); */
@Deprecated
public class SOSHibernateStatelessSession extends SOSHibernateSession {

    private static final long serialVersionUID = 1L;

    protected SOSHibernateStatelessSession(SOSHibernateFactory factory) {
        super(factory);
        this.setUseOpenStatelessSession(true);
    }

}
