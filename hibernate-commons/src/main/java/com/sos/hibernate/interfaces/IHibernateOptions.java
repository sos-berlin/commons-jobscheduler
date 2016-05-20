package com.sos.hibernate.interfaces;

import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionDBDriver;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionJdbcUrl;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionString;

public interface IHibernateOptions {

    public abstract SOSOptionString getHibernateConnectionIsolation();

    public abstract void setHibernateConnectionIsolation(SOSOptionString pHibernateConnectionIsolation);

    public abstract SOSOptionBoolean getHibernateConnectionAutocommit();

    public abstract void setHibernateConnectionAutocommit(SOSOptionBoolean pHibernateConnectionAutocommit);

    public abstract SOSOptionInFileName getHibernateConnectionConfigFile();

    public abstract void setHibernateConnectionConfigFile(SOSOptionInFileName pHibernateConnectionConfigFile);

    public abstract SOSOptionDBDriver getHibernateConnectionDriverClass();

    public abstract void setHibernateConnectionDriverClass(SOSOptionDBDriver pHibernateConnectionDriverClass);

    public abstract SOSOptionPassword getHibernateConnectionPassword();

    public abstract void setHibernateConnectionPassword(SOSOptionPassword pHibernateConnectionPassword);

    public abstract SOSOptionJdbcUrl getHibernateConnectionUrl();

    public abstract void setHibernateConnectionUrl(SOSOptionJdbcUrl pHibernateConnectionUrl);

    public abstract SOSOptionString getHibernateConnectionUsername();

    public abstract void setHibernateConnectionUsername(SOSOptionString pHibernateConnectionUsername);

    public abstract SOSOptionString getHibernateDialect();

    public abstract void setHibernateDialect(SOSOptionString pHibernateDialect);

    public abstract SOSOptionBoolean getHibernateFormatSql();

    public abstract void setHibernateFormatSql(SOSOptionBoolean pHibernateFormatSql);

    public abstract SOSOptionBoolean getHibernateShowSql();

    public abstract void setHibernateShowSql(SOSOptionBoolean pHibernateShowSql);

    public abstract SOSOptionBoolean getHibernateJdbcUseScrollableResultset();

    public abstract void setHibernateJdbcUseScrollableResultset(SOSOptionBoolean pHibernateJdbcUseScrollableResultset);

}