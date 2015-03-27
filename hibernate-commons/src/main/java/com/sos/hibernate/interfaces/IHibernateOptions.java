package com.sos.hibernate.interfaces;

import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionDBDriver;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionJdbcUrl;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionString;

public interface IHibernateOptions {

    /**
     * \brief gethibernate_connection_isolation : 
     * 
     * \details
     * 
     *
     * \return 
     *
     */
    public abstract SOSOptionString gethibernate_connection_isolation();

    /**
     * \brief sethibernate_connection_isolation : 
     * 
     * \details
     * 
     *
     * @param hibernate_connection_isolation : 
     */
    public abstract void sethibernate_connection_isoalation(
            SOSOptionString p_hibernate_connection_isolation);

    
    
	/**
	 * \brief gethibernate_connection_autocommit : 
	 * 
	 * \details
	 * 
	 *
	 * \return 
	 *
	 */
	public abstract SOSOptionBoolean gethibernate_connection_autocommit();

	/**
	 * \brief sethibernate_connection_autocommit : 
	 * 
	 * \details
	 * 
	 *
	 * @param hibernate_connection_autocommit : 
	 */
	public abstract void sethibernate_connection_autocommit(
			SOSOptionBoolean p_hibernate_connection_autocommit);

	/**
	 * \brief gethibernate_connection_config_file : 
	 * 
	 * \details
	 * Hibernate configuration file of the database connection
	 *
	 * \return 
	 *
	 */
	public abstract SOSOptionInFileName gethibernate_connection_config_file();

	/**
	 * \brief sethibernate_connection_config_file : 
	 * 
	 * \details
	 * Hibernate configuration file of the database connection
	 *
	 * @param hibernate_connection_config_file : 
	 */
	public abstract void sethibernate_connection_config_file(
			SOSOptionInFileName p_hibernate_connection_config_file);

	/**
	 * \brief gethibernate_connection_driver_class : 
	 * 
	 * \details
	 * Class of JBDC driver of the database connection
	 *
	 * \return 
	 *
	 */
	public abstract SOSOptionDBDriver gethibernate_connection_driver_class();

	/**
	 * \brief sethibernate_connection_driver_class : 
	 * 
	 * \details
	 * Class of JBDC driver of the database connection
	 *
	 * @param hibernate_connection_driver_class : 
	 */
	public abstract void sethibernate_connection_driver_class(
			SOSOptionDBDriver p_hibernate_connection_driver_class);

	/**
	 * \brief gethibernate_connection_password : 
	 * 
	 * \details
	 * Password of the database connection
	 *
	 * \return 
	 *
	 */
	public abstract SOSOptionPassword gethibernate_connection_password();

	/**
	 * \brief sethibernate_connection_password : 
	 * 
	 * \details
	 * Password of the database connection
	 *
	 * @param hibernate_connection_password : 
	 */
	public abstract void sethibernate_connection_password(
			SOSOptionPassword p_hibernate_connection_password);

	/**
	 * \brief gethibernate_connection_url : 
	 * 
	 * \details
	 * JDBC URL of the database connection
	 *
	 * \return 
	 *
	 */
	public abstract SOSOptionJdbcUrl gethibernate_connection_url();

	/**
	 * \brief sethibernate_connection_url : 
	 * 
	 * \details
	 * JDBC URL of the database connection
	 *
	 * @param hibernate_connection_url : 
	 */
	public abstract void sethibernate_connection_url(
			SOSOptionJdbcUrl p_hibernate_connection_url);

	/**
	 * \brief gethibernate_connection_username : 
	 * 
	 * \details
	 * User of the database connection
	 *
	 * \return 
	 *
	 */
	public abstract SOSOptionString gethibernate_connection_username();

	/**
	 * \brief sethibernate_connection_username : 
	 * 
	 * \details
	 * User of the database connection
	 *
	 * @param hibernate_connection_username : 
	 */
	public abstract void sethibernate_connection_username(
			SOSOptionString p_hibernate_connection_username);

	/**
	 * \brief gethibernate_dialect : 
	 * 
	 * \details
	 * Hibernate dialect of the database connection
	 *
	 * \return 
	 *
	 */
	public abstract SOSOptionString gethibernate_dialect();

	/**
	 * \brief sethibernate_dialect : 
	 * 
	 * \details
	 * Hibernate dialect of the database connection
	 *
	 * @param hibernate_dialect : 
	 */
	public abstract void sethibernate_dialect(
			SOSOptionString p_hibernate_dialect);

	/**
	 * \brief gethibernate_format_sql : 
	 * 
	 * \details
	 * 
	 *
	 * \return 
	 *
	 */
	public abstract SOSOptionBoolean gethibernate_format_sql();

	/**
	 * \brief sethibernate_format_sql : 
	 * 
	 * \details
	 * 
	 *
	 * @param hibernate_format_sql : 
	 */
	public abstract void sethibernate_format_sql(
			SOSOptionBoolean p_hibernate_format_sql);

	/**
	 * \brief gethibernate_show_sql : 
	 * 
	 * \details
	 * 
	 *
	 * \return 
	 *
	 */
	public abstract SOSOptionBoolean gethibernate_show_sql();

	/**
	 * \brief sethibernate_show_sql : 
	 * 
	 * \details
	 * 
	 *
	 * @param hibernate_show_sql : 
	 */
	public abstract void sethibernate_show_sql(
			SOSOptionBoolean p_hibernate_show_sql);
	
	/**
	 * \brief gethibernate_jdbc_use_scrollable_resultset : 
	 * 
	 * \details
	 * 
	 *
	 * \return 
	 *
	 */
	public abstract SOSOptionBoolean gethibernate_jdbc_use_scrollable_resultset();

	/**
	 * \brief sethibernate_jdbc_use_scrollable_resultset : 
	 * 
	 * \details
	 * 
	 *
	 * @param hibernate_jdbc_use_scrollable_resultset : 
	 */
	public abstract void sethibernate_jdbc_use_scrollable_resultset(
			SOSOptionBoolean p_hibernate_jdbc_use_scrollable_resultset);
}