package com.sos.hibernate.options;

import java.util.HashMap;

import org.apache.log4j.Logger;

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
import com.sos.JSHelper.Archiver.IJSArchiverOptions;

@JSOptionClass(name = "JobNetOptionsSuperClass", description = "JobNetOptionsSuperClass")
public class HibernateOptions extends JSOptionsClass implements IHibernateOptions {

    /**
	 * 
	 */
    private static final long serialVersionUID = 5713555021974034071L;
    private final String conClassName = "JSHibernateOptions";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(HibernateOptions.class);

    /** \var hibernate_connection_isolation : */
    @JSOptionDefinition(name = "hibernate_connection_isolation", description = "", key = "hibernate_connection_isolation", type = "SOSOptionString", mandatory = false)
    public SOSOptionString hibernate_connection_isolation = new SOSOptionString(this, conClassName + ".hibernate_connection_isolation", // HashMap-Key
            "", // Titel
            "2", // InitValue
            "2", // DefaultValue
            false // isMandatory
            );

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * gethibernate_connection_autocommit()
     */
    @Override
    public SOSOptionString gethibernate_connection_isolation() {
        return hibernate_connection_isolation;
    }

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * sethibernate_connection_autocommit
     * (com.sos.JSHelper.Options.SOSOptionBoolean)
     */
    @Override
    public void sethibernate_connection_isoalation(SOSOptionString p_hibernate_connection_isolation) {
        this.hibernate_connection_isolation = p_hibernate_connection_isolation;
    }

    /** \var hibernate_connection_autocommit : */
    @JSOptionDefinition(name = "hibernate_connection_autocommit", description = "", key = "hibernate_connection_autocommit", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernate_connection_autocommit = new SOSOptionBoolean(this, conClassName + ".hibernate_connection_autocommit", // HashMap-Key
            "", // Titel
            "false", // InitValue
            "false", // DefaultValue
            false // isMandatory
            );

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * gethibernate_connection_autocommit()
     */
    @Override
    public SOSOptionBoolean gethibernate_connection_autocommit() {
        return hibernate_connection_autocommit;
    }

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * sethibernate_connection_autocommit
     * (com.sos.JSHelper.Options.SOSOptionBoolean)
     */
    @Override
    public void sethibernate_connection_autocommit(SOSOptionBoolean p_hibernate_connection_autocommit) {
        this.hibernate_connection_autocommit = p_hibernate_connection_autocommit;
    }

    /** \var hibernate_connection_config_file : Hibernate configuration file of
     * the database connection */
    @JSOptionDefinition(name = "hibernate_connection_config_file", description = "", key = "hibernate_connection_config_file", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName hibernate_connection_config_file = new SOSOptionInFileName(this, conClassName + ".hibernate_connection_config_file", // HashMap-Key
            "", // Titel
            "config/hibernate.cfg.xml", // InitValue
            "config/hibernate.cfg.xml", // DefaultValue
            false // isMandatory
            );

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * gethibernate_connection_config_file()
     */
    @Override
    public SOSOptionInFileName gethibernate_connection_config_file() {
        return hibernate_connection_config_file;
    }

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * sethibernate_connection_config_file
     * (com.sos.JSHelper.Options.SOSOptionInFileName)
     */
    @Override
    public void sethibernate_connection_config_file(SOSOptionInFileName p_hibernate_connection_config_file) {
        this.hibernate_connection_config_file = p_hibernate_connection_config_file;
    }

    /** \var hibernate_connection_driver_class : Class of JBDC driver of the
     * database connection */
    @JSOptionDefinition(name = "hibernate_connection_driver_class", description = "", key = "hibernate_connection_driver_class", type = "SOSOptionDBDriver", mandatory = false)
    public SOSOptionDBDriver hibernate_connection_driver_class = new SOSOptionDBDriver(this, conClassName + ".hibernate_connection_driver_class", // HashMap-Key
            "", // Titel
            "oracle.jdbc.driver.OracleDriver", // InitValue
            "oracle.jdbc.driver.OracleDriver", // DefaultValue
            false // isMandatory
            );

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * gethibernate_connection_driver_class()
     */
    @Override
    public SOSOptionDBDriver gethibernate_connection_driver_class() {
        return hibernate_connection_driver_class;
    }

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * sethibernate_connection_driver_class
     * (com.sos.JSHelper.Options.SOSOptionDBDriver)
     */
    @Override
    public void sethibernate_connection_driver_class(SOSOptionDBDriver p_hibernate_connection_driver_class) {
        this.hibernate_connection_driver_class = p_hibernate_connection_driver_class;
    }

    /** \var hibernate_connection_password : Password of the database connection */
    @JSOptionDefinition(name = "hibernate_connection_password", description = "", key = "hibernate_connection_password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword hibernate_connection_password = new SOSOptionPassword(this, conClassName + ".hibernate_connection_password", // HashMap-Key
            "", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
            );

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * gethibernate_connection_password()
     */
    @Override
    public SOSOptionPassword gethibernate_connection_password() {
        return hibernate_connection_password;
    }

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * sethibernate_connection_password
     * (com.sos.JSHelper.Options.SOSOptionPassword)
     */
    @Override
    public void sethibernate_connection_password(SOSOptionPassword p_hibernate_connection_password) {
        this.hibernate_connection_password = p_hibernate_connection_password;
    }

    /** \var hibernate_connection_url : JDBC URL of the database connection */
    @JSOptionDefinition(name = "hibernate_connection_url", description = "", key = "hibernate_connection_url", type = "SOSOptionJdbcUrl", mandatory = false)
    public SOSOptionJdbcUrl hibernate_connection_url = new SOSOptionJdbcUrl(this, conClassName + ".hibernate_connection_url", // HashMap-Key
            "", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
            );

    /*
     * (non-Javadoc)
     * @see
     * com.sos.jobnet.options.ISOSJSHibernateOptions#gethibernate_connection_url
     * ()
     */
    @Override
    public SOSOptionJdbcUrl gethibernate_connection_url() {
        return hibernate_connection_url;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.sos.jobnet.options.ISOSJSHibernateOptions#sethibernate_connection_url
     * (com.sos.JSHelper.Options.SOSOptionJdbcUrl)
     */
    @Override
    public void sethibernate_connection_url(SOSOptionJdbcUrl p_hibernate_connection_url) {
        this.hibernate_connection_url = p_hibernate_connection_url;
    }

    /** \var hibernate_connection_username : User of the database connection */
    @JSOptionDefinition(name = "hibernate_connection_username", description = "", key = "hibernate_connection_username", type = "SOSOptionString", mandatory = false)
    public SOSOptionString hibernate_connection_username = new SOSOptionString(this, conClassName + ".hibernate_connection_username", // HashMap-Key
            "", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
            );

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * gethibernate_connection_username()
     */
    @Override
    public SOSOptionString gethibernate_connection_username() {
        return hibernate_connection_username;
    }

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * sethibernate_connection_username
     * (com.sos.JSHelper.Options.SOSOptionString)
     */
    @Override
    public void sethibernate_connection_username(SOSOptionString p_hibernate_connection_username) {
        this.hibernate_connection_username = p_hibernate_connection_username;
    }

    /** \var hibernate_dialect : Hibernate dialect of the database connection */
    @JSOptionDefinition(name = "hibernate_dialect", description = "", key = "hibernate_dialect", type = "SOSOptionString", mandatory = false)
    public SOSOptionString hibernate_dialect = new SOSOptionString(this, conClassName + ".hibernate_dialect", // HashMap-Key
            "", // Titel
            "org.hibernate.dialect.Oracle10gDialect", // InitValue
            "org.hibernate.dialect.Oracle10gDialect", // DefaultValue
            false // isMandatory
            );

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#gethibernate_dialect()
     */
    @Override
    public SOSOptionString gethibernate_dialect() {
        return hibernate_dialect;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.sos.jobnet.options.ISOSJSHibernateOptions#sethibernate_dialect(com
     * .sos.JSHelper.Options.SOSOptionString)
     */
    @Override
    public void sethibernate_dialect(SOSOptionString p_hibernate_dialect) {
        this.hibernate_dialect = p_hibernate_dialect;
    }

    /** \var hibernate_format_sql : */
    @JSOptionDefinition(name = "hibernate_format_sql", description = "", key = "hibernate_format_sql", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernate_format_sql = new SOSOptionBoolean(this, conClassName + ".hibernate_format_sql", // HashMap-Key
            "", // Titel
            "false", // InitValue
            "false", // DefaultValue
            false // isMandatory
            );

    /*
     * (non-Javadoc)
     * @see
     * com.sos.jobnet.options.ISOSJSHibernateOptions#gethibernate_format_sql()
     */
    @Override
    public SOSOptionBoolean gethibernate_format_sql() {
        return hibernate_format_sql;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.sos.jobnet.options.ISOSJSHibernateOptions#sethibernate_format_sql
     * (com.sos.JSHelper.Options.SOSOptionBoolean)
     */
    @Override
    public void sethibernate_format_sql(SOSOptionBoolean p_hibernate_format_sql) {
        this.hibernate_format_sql = p_hibernate_format_sql;
    }

    /** \var hibernate_show_sql : */
    @JSOptionDefinition(name = "hibernate_show_sql", description = "", key = "hibernate_show_sql", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernate_show_sql = new SOSOptionBoolean(this, conClassName + ".hibernate_show_sql", // HashMap-Key
            "", // Titel
            "false", // InitValue
            "false", // DefaultValue
            false // isMandatory
            );

    /*
     * (non-Javadoc)
     * @see
     * com.sos.jobnet.options.ISOSJSHibernateOptions#gethibernate_show_sql()
     */
    @Override
    public SOSOptionBoolean gethibernate_show_sql() {
        return hibernate_show_sql;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.sos.jobnet.options.ISOSJSHibernateOptions#sethibernate_show_sql(com
     * .sos.JSHelper.Options.SOSOptionBoolean)
     */
    @Override
    public void sethibernate_show_sql(SOSOptionBoolean p_hibernate_show_sql) {
        this.hibernate_show_sql = p_hibernate_show_sql;
    }

    /** \var hibernate_jdbc_use_scrollable_resultset : */
    @JSOptionDefinition(name = "hibernate_jdbc_use_scrollable_resultset", description = "", key = "hibernate_jdbc_use_scrollable_resultset", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean hibernate_jdbc_use_scrollable_resultset = new SOSOptionBoolean(this, conClassName
            + ".hibernate_jdbc_use_scrollable_resultset", // HashMap-Key
            "", // Titel
            "true", // InitValue
            "true", // DefaultValue
            false // isMandatory
            );

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * gethibernate_jdbc_use_scrollable_resultset()
     */
    @Override
    public SOSOptionBoolean gethibernate_jdbc_use_scrollable_resultset() {
        return hibernate_jdbc_use_scrollable_resultset;
    }

    /*
     * (non-Javadoc)
     * @see com.sos.jobnet.options.ISOSJSHibernateOptions#
     * sethibernate_jdbc_use_scrollable_resultset
     * (com.sos.JSHelper.Options.SOSOptionBoolean)
     */
    @Override
    public void sethibernate_jdbc_use_scrollable_resultset(SOSOptionBoolean p_hibernate_jdbc_use_scrollable_resultset) {
        this.hibernate_jdbc_use_scrollable_resultset = p_hibernate_jdbc_use_scrollable_resultset;
    }

    public HibernateOptions() {
        objParentClass = this.getClass();
    } // public JobNetOptionsSuperClass

    //
    public HibernateOptions(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    } // public JobNetOptionsSuperClass (HashMap JSSettings)

    /** \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
     * Optionen als String
     * 
     * \details
     * 
     * \see toString \see toOut */
    private String getAllOptionsAsString() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getAllOptionsAsString";
        String strT = conClassName + "\n";
        final StringBuffer strBuffer = new StringBuffer();
        // strT += IterateAllDataElementsByAnnotation(objParentClass, this,
        // JSOptionsClass.IterationTypes.toString, strBuffer);
        // strT += IterateAllDataElementsByAnnotation(objParentClass, this, 13,
        // strBuffer);
        strT += this.toString(); // fix
        //
        return strT;
    } // private String getAllOptionsAsString ()

    /** \brief setAllOptions - übernimmt die OptionenWerte aus der HashMap
     * 
     * \details In der als Parameter anzugebenden HashMap sind Schlüssel (Name)
     * und Wert der jeweiligen Option als Paar angegeben. Ein Beispiel für den
     * Aufbau einer solchen HashMap findet sich in der Beschreibung dieser
     * Klasse (\ref TestData "setJobSchedulerSSHJobOptions"). In dieser Routine
     * werden die Schlüssel analysiert und, falls gefunden, werden die
     * dazugehörigen Werte den Properties dieser Klasse zugewiesen.
     * 
     * Nicht bekannte Schlüssel werden ignoriert.
     * 
     * \see JSOptionsClass::getItem
     * 
     * @param pobjJSSettings
     * @throws Exception */
    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setAllOptions";
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    } // public void setAllOptions (HashMap <String, String> JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     * 
     * \details
     * 
     * @throws Exception
     * 
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing //
            , Exception {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()

    /** \brief CommandLineArgs - Übernehmen der Options/Settings aus der
     * Kommandozeile
     * 
     * \details Die in der Kommandozeile beim Starten der Applikation
     * angegebenen Parameter werden hier in die HashMap übertragen und danach
     * den Optionen als Wert zugewiesen.
     * 
     * \return void
     * 
     * @param pstrArgs
     * @throws Exception */
    @Override
    public void commandLineArgs(String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

} // public class JobNetOptionsSuperClass