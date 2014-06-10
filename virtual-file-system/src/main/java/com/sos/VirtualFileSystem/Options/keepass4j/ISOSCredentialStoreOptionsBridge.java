/**
 * 
 */
package com.sos.VirtualFileSystem.Options.keepass4j;
import java.util.HashMap;

import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionPassphrase;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUrl;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;

/**
 * @author KB
 *
 */
public interface ISOSCredentialStoreOptionsBridge {
	public abstract SOSOptionUrl getUrl(); // public String geturl

	public abstract void setUrl(SOSOptionUrl pstrValue); // public SOSConnection2OptionsSuperClass seturl

	/**
	 * \brief gethost : Host-Name This parameter specifies th
	 *
	 * \details
	 * This parameter specifies the hostname or IP address of the server to which a connection has to be made.
	 *
	 * \return Host-Name This parameter specifies th
	 *
	 */
	public abstract SOSOptionHostName getHost();

	/**
	 * \brief sethost : Host-Name This parameter specifies th
	 *
	 * \details
	 * This parameter specifies the hostname or IP address of the server to which a connection has to be made.
	 *
	 * @param host : Host-Name This parameter specifies th
	 */
	public abstract void setHost(SOSOptionHostName p_host);

	/**
	 * \brief getport : Port-Number to be used for Data-Transfer
	 *
	 * \details
	 * Port by which files should be transferred. For FTP this is usually port 21, for SFTP this is usually port 22.
	 *
	 * \return Port-Number to be used for Data-Transfer
	 *
	 */
	public abstract SOSOptionPortNumber getPort();

	/**
	 * \brief setport : Port-Number to be used for Data-Transfer
	 *
	 * \details
	 * Port by which files should be transferred. For FTP this is usually port 21, for SFTP this is usually port 22.
	 *
	 * @param port : Port-Number to be used for Data-Transfer
	 */
	public abstract void setPort(SOSOptionPortNumber p_port);

	/**
	 * \brief getprotocol : Type of requested Datatransfer The values ftp, sftp
	 *
	 * \details
	 * The values ftp, sftp or ftps are valid for this parameter. If sftp is used, then the ssh_* parameters will be applied.
	 *
	 * \return Type of requested Datatransfer The values ftp, sftp
	 *
	 */
	public abstract SOSOptionTransferType getProtocol();

	/**
	 * \brief setprotocol : Type of requested Datatransfer The values ftp, sftp
	 *
	 * \details
	 * The values ftp, sftp or ftps are valid for this parameter. If sftp is used, then the ssh_* parameters will be applied.
	 *
	 * @param protocol : Type of requested Datatransfer The values ftp, sftp
	 */
	public abstract void setProtocol(SOSOptionTransferType p_protocol);

	/**
	 * \brief getuser
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	public abstract SOSOptionUserName getUser();

	/**
	 * \brief getpassword
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	public abstract SOSOptionPassword getPassword();

	/**
	 * \brief setpassword
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_password
	 */
	public abstract void setPassword(SOSOptionPassword p_password);

	/**
	 * \brief getssh_auth_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	public abstract SOSOptionInFileName getAuth_file();

	/**
	 * \brief setssh_auth_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_ssh_auth_file
	 */
	public abstract void setAuth_file(SOSOptionInFileName p_ssh_auth_file);

	/**
	 * \brief getssh_auth_method
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	public abstract SOSOptionAuthenticationMethod getAuth_method();

	/**
	 * \brief setssh_auth_method
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_ssh_auth_method
	 */
	public abstract void setAuth_method(SOSOptionAuthenticationMethod p_ssh_auth_method);

	/**
	 * \brief setUser
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pobjUser
	 */
	public abstract void setUser(SOSOptionUserName pobjUser);

	public abstract void CommandLineArgs(String[] strArgs);

	public abstract void CommandLineArgs(String notesText);
}
