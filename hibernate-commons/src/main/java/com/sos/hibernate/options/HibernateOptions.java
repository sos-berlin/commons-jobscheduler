package com.sos.hibernate.options;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionDBDriver;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionJdbcUrl;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.hibernate.interfaces.IHibernateOptions;

@JSOptionClass(name = "JobNetOptionsSuperClass", description = "JobNetOptionsSuperClass")
public class HibernateOptions extends JSOptionsClass implements IHibernateOptions {

    private static final long serialVersionUID = 5713555021974034071L;
    private static final String CLASSNAME = "JSHibernateOptions";

    @JSOptionDefinition(name = "hibernate_connection_isolation", description = "", key = "hibernate_connection_isolation", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString hibernateConnectionIsolation = new SOSOptionString(this, conClassName + ".hibernate_connection_isolation",
            "", "2", "2", false);

    @Override
    public SOSOptionString getHibernateConnectionIsolation() {
        return hibernateConnectionIsolation;
    }

    @Override
    public void setHibernateConnectionIsolation(SOSOptionString pHibernateConnectionIsolation) {
        this.hibernateConnectionIsolation = pHibernateConnectionIsolation;
    }

    @JSOptionDefinition(name = "hibernate_connection_autocommit", description = "", key = "hibernate_connection_autocommit",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernateConnectionAutocommit = new SOSOptionBoolean(this, conClassName + ".hibernate_connection_autocommit",
            "", "false", "false", false);

    @Override
    public SOSOptionBoolean getHibernateConnectionAutocommit() {
        return hibernateConnectionAutocommit;
    }

    @Override
    public void setHibernateConnectionAutocommit(SOSOptionBoolean pHibernateConnectionAutocommit) {
        this.hibernateConnectionAutocommit = pHibernateConnectionAutocommit;
    }

    @JSOptionDefinition(name = "hibernate_connection_config_file", description = "", key = "hibernate_connection_config_file",
            type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName hibernateConnectionConfigFile = new SOSOptionInFileName(this, conClassName + ".hibernate_connection_config_file",
            "", "config/hibernate.cfg.xml", "config/hibernate.cfg.xml", false);

    @Override
    public SOSOptionInFileName getHibernateConnectionConfigFile() {
        return hibernateConnectionConfigFile;
    }

    @Override
    public void setHibernateConnectionConfigFile(SOSOptionInFileName pHibernateConnectionConfigFile) {
        this.hibernateConnectionConfigFile = pHibernateConnectionConfigFile;
    }

    @JSOptionDefinition(name = "hibernate_connection_driver_class", description = "", key = "hibernate_connection_driver_class",
            type = "SOSOptionDBDriver", mandatory = false)
    public SOSOptionDBDriver hibernateConnectionDriverClass = new SOSOptionDBDriver(this, conClassName + ".hibernate_connection_driver_class",
            "", "oracle.jdbc.driver.OracleDriver", "oracle.jdbc.driver.OracleDriver", false);

    @Override
    public SOSOptionDBDriver getHibernateConnectionDriverClass() {
        return hibernateConnectionDriverClass;
    }

    @Override
    public void setHibernateConnectionDriverClass(SOSOptionDBDriver pHibernateConnectionDriverClass) {
        this.hibernateConnectionDriverClass = pHibernateConnectionDriverClass;
    }

    @JSOptionDefinition(name = "hibernate_connection_password", description = "", key = "hibernate_connection_password", type = "SOSOptionPassword",
            mandatory = false)
    public SOSOptionPassword hibernateConnectionPassword = new SOSOptionPassword(this, conClassName + ".hibernate_connection_password",
            "", "", "", false);

    @Override
    public SOSOptionPassword getHibernateConnectionPassword() {
        return hibernateConnectionPassword;
    }

    @Override
    public void setHibernateConnectionPassword(SOSOptionPassword pHibernateConnectionPassword) {
        this.hibernateConnectionPassword = pHibernateConnectionPassword;
    }

    @JSOptionDefinition(name = "hibernate_connection_url", description = "", key = "hibernate_connection_url", type = "SOSOptionJdbcUrl",
            mandatory = false)
    public SOSOptionJdbcUrl hibernateConnectionUrl = new SOSOptionJdbcUrl(this, conClassName + ".hibernate_connection_url", "", "", "", false);

    @Override
    public SOSOptionJdbcUrl getHibernateConnectionUrl() {
        return hibernateConnectionUrl;
    }

    @Override
    public void setHibernateConnectionUrl(SOSOptionJdbcUrl pHibernateConnectionUrl) {
        this.hibernateConnectionUrl = pHibernateConnectionUrl;
    }

    @JSOptionDefinition(name = "hibernate_connection_username", description = "", key = "hibernate_connection_username", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString hibernateConnectionUsername = new SOSOptionString(this, conClassName + ".hibernate_connection_username", "", "", "",
            false);

    @Override
    public SOSOptionString getHibernateConnectionUsername() {
        return hibernateConnectionUsername;
    }

    @Override
    public void setHibernateConnectionUsername(SOSOptionString pHibernateConnectionUsername) {
        this.hibernateConnectionUsername = pHibernateConnectionUsername;
    }

    @JSOptionDefinition(name = "hibernate_dialect", description = "", key = "hibernate_dialect", type = "SOSOptionString", mandatory = false)
    public SOSOptionString hibernateDialect = new SOSOptionString(this, conClassName + ".hibernate_dialect", "",
            "org.hibernate.dialect.Oracle10gDialect", "org.hibernate.dialect.Oracle10gDialect", false);

    @Override
    public SOSOptionString getHibernateDialect() {
        return hibernateDialect;
    }

    @Override
    public void setHibernateDialect(SOSOptionString pHibernateDialect) {
        this.hibernateDialect = pHibernateDialect;
    }

    @JSOptionDefinition(name = "hibernate_format_sql", description = "", key = "hibernate_format_sql", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernateFormatSql = new SOSOptionBoolean(this, conClassName + ".hibernate_format_sql", "", "false", "false", false);

    @Override
    public SOSOptionBoolean getHibernateFormatSql() {
        return hibernateFormatSql;
    }

    @Override
    public void setHibernateFormatSql(SOSOptionBoolean pHibernateFormatSql) {
        this.hibernateFormatSql = pHibernateFormatSql;
    }

    @JSOptionDefinition(name = "hibernate_show_sql", description = "", key = "hibernate_show_sql", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernateShowSql = new SOSOptionBoolean(this, conClassName + ".hibernate_show_sql", "", "false", "false", false);

    @Override
    public SOSOptionBoolean getHibernateShowSql() {
        return hibernateShowSql;
    }

    @Override
    public void setHibernateShowSql(SOSOptionBoolean p_hibernate_show_sql) {
        this.hibernateShowSql = p_hibernate_show_sql;
    }

    @JSOptionDefinition(name = "hibernate_jdbc_use_scrollable_resultset", description = "", key = "hibernate_jdbc_use_scrollable_resultset",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernateJdbcUseScrollableResultset = new SOSOptionBoolean(this, conClassName
            + ".hibernate_jdbc_use_scrollable_resultset", "", "true", "true", false);

    @Override
    public SOSOptionBoolean getHibernateJdbcUseScrollableResultset() {
        return hibernateJdbcUseScrollableResultset;
    }

    @Override
    public void setHibernateJdbcUseScrollableResultset(SOSOptionBoolean val) {
        this.hibernateJdbcUseScrollableResultset = val;
    }

    public HibernateOptions() {
        objParentClass = this.getClass();
    }

    public HibernateOptions(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> settings) {
        super.setAllOptions(settings);
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.getSettings());
    }

}