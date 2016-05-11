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

    @JSOptionDefinition(name = "hibernate_connection_isolation", description = "", key = "hibernate_connection_isolation", type = "SOSOptionString", mandatory = false)
    public SOSOptionString hibernate_connection_isolation = new SOSOptionString(this, CLASSNAME + ".hibernate_connection_isolation", "", "2", "2",
            false);

    @Override
    public SOSOptionString gethibernate_connection_isolation() {
        return hibernate_connection_isolation;
    }

    @Override
    public void sethibernate_connection_isoalation(SOSOptionString p_hibernate_connection_isolation) {
        this.hibernate_connection_isolation = p_hibernate_connection_isolation;
    }

    @JSOptionDefinition(name = "hibernate_connection_autocommit", description = "", key = "hibernate_connection_autocommit", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernate_connection_autocommit = new SOSOptionBoolean(this, CLASSNAME + ".hibernate_connection_autocommit", "", "false",
            "false", false);

    @Override
    public SOSOptionBoolean gethibernate_connection_autocommit() {
        return hibernate_connection_autocommit;
    }

    @Override
    public void sethibernate_connection_autocommit(SOSOptionBoolean p_hibernate_connection_autocommit) {
        this.hibernate_connection_autocommit = p_hibernate_connection_autocommit;
    }

    @JSOptionDefinition(name = "hibernate_connection_config_file", description = "", key = "hibernate_connection_config_file", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName hibernate_connection_config_file = new SOSOptionInFileName(this, CLASSNAME + ".hibernate_connection_config_file", "",
            "config/hibernate.cfg.xml", "config/hibernate.cfg.xml", false);

    @Override
    public SOSOptionInFileName gethibernate_connection_config_file() {
        return hibernate_connection_config_file;
    }

    @Override
    public void sethibernate_connection_config_file(SOSOptionInFileName p_hibernate_connection_config_file) {
        this.hibernate_connection_config_file = p_hibernate_connection_config_file;
    }

    @JSOptionDefinition(name = "hibernate_connection_driver_class", description = "", key = "hibernate_connection_driver_class", type = "SOSOptionDBDriver", mandatory = false)
    public SOSOptionDBDriver hibernate_connection_driver_class = new SOSOptionDBDriver(this, CLASSNAME + ".hibernate_connection_driver_class", "",
            "oracle.jdbc.driver.OracleDriver", "oracle.jdbc.driver.OracleDriver", false);

    @Override
    public SOSOptionDBDriver gethibernate_connection_driver_class() {
        return hibernate_connection_driver_class;
    }

    @Override
    public void sethibernate_connection_driver_class(SOSOptionDBDriver p_hibernate_connection_driver_class) {
        this.hibernate_connection_driver_class = p_hibernate_connection_driver_class;
    }

    @JSOptionDefinition(name = "hibernate_connection_password", description = "", key = "hibernate_connection_password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword hibernate_connection_password = new SOSOptionPassword(this, CLASSNAME + ".hibernate_connection_password", "", "", "",
            false);

    @Override
    public SOSOptionPassword gethibernate_connection_password() {
        return hibernate_connection_password;
    }

    @Override
    public void sethibernate_connection_password(SOSOptionPassword p_hibernate_connection_password) {
        this.hibernate_connection_password = p_hibernate_connection_password;
    }

    @JSOptionDefinition(name = "hibernate_connection_url", description = "", key = "hibernate_connection_url", type = "SOSOptionJdbcUrl", mandatory = false)
    public SOSOptionJdbcUrl hibernate_connection_url = new SOSOptionJdbcUrl(this, CLASSNAME + ".hibernate_connection_url", "", "", "", false);

    @Override
    public SOSOptionJdbcUrl gethibernate_connection_url() {
        return hibernate_connection_url;
    }

    @Override
    public void sethibernate_connection_url(SOSOptionJdbcUrl p_hibernate_connection_url) {
        this.hibernate_connection_url = p_hibernate_connection_url;
    }

    @JSOptionDefinition(name = "hibernate_connection_username", description = "", key = "hibernate_connection_username", type = "SOSOptionString", mandatory = false)
    public SOSOptionString hibernate_connection_username = new SOSOptionString(this, CLASSNAME + ".hibernate_connection_username", "", "", "", false);

    @Override
    public SOSOptionString gethibernate_connection_username() {
        return hibernate_connection_username;
    }

    @Override
    public void sethibernate_connection_username(SOSOptionString p_hibernate_connection_username) {
        this.hibernate_connection_username = p_hibernate_connection_username;
    }

    @JSOptionDefinition(name = "hibernate_dialect", description = "", key = "hibernate_dialect", type = "SOSOptionString", mandatory = false)
    public SOSOptionString hibernate_dialect = new SOSOptionString(this, CLASSNAME + ".hibernate_dialect", "",
            "org.hibernate.dialect.Oracle10gDialect", "org.hibernate.dialect.Oracle10gDialect", false);

    @Override
    public SOSOptionString gethibernate_dialect() {
        return hibernate_dialect;
    }

    @Override
    public void sethibernate_dialect(SOSOptionString p_hibernate_dialect) {
        this.hibernate_dialect = p_hibernate_dialect;
    }

    @JSOptionDefinition(name = "hibernate_format_sql", description = "", key = "hibernate_format_sql", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernate_format_sql = new SOSOptionBoolean(this, CLASSNAME + ".hibernate_format_sql", "", "false", "false", false);

    @Override
    public SOSOptionBoolean gethibernate_format_sql() {
        return hibernate_format_sql;
    }

    @Override
    public void sethibernate_format_sql(SOSOptionBoolean p_hibernate_format_sql) {
        this.hibernate_format_sql = p_hibernate_format_sql;
    }

    @JSOptionDefinition(name = "hibernate_show_sql", description = "", key = "hibernate_show_sql", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernate_show_sql = new SOSOptionBoolean(this, CLASSNAME + ".hibernate_show_sql", "", "false", "false", false);

    @Override
    public SOSOptionBoolean gethibernate_show_sql() {
        return hibernate_show_sql;
    }

    @Override
    public void sethibernate_show_sql(SOSOptionBoolean p_hibernate_show_sql) {
        this.hibernate_show_sql = p_hibernate_show_sql;
    }

    @JSOptionDefinition(name = "hibernate_jdbc_use_scrollable_resultset", description = "", key = "hibernate_jdbc_use_scrollable_resultset", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernate_jdbc_use_scrollable_resultset = new SOSOptionBoolean(this, CLASSNAME
            + ".hibernate_jdbc_use_scrollable_resultset", "", "true", "true", false);

    @Override
    public SOSOptionBoolean gethibernate_jdbc_use_scrollable_resultset() {
        return hibernate_jdbc_use_scrollable_resultset;
    }

    @Override
    public void sethibernate_jdbc_use_scrollable_resultset(SOSOptionBoolean p_hibernate_jdbc_use_scrollable_resultset) {
        this.hibernate_jdbc_use_scrollable_resultset = p_hibernate_jdbc_use_scrollable_resultset;
    }

    public HibernateOptions() {
        objParentClass = this.getClass();
    }

    public HibernateOptions(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        objSettings = pobjJSSettings;
        super.setAllOptions(pobjJSSettings);
    }

    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

}
