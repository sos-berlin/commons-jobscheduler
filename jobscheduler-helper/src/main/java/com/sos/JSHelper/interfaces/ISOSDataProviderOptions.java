package com.sos.JSHelper.interfaces;

import java.util.HashMap;

import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUrl;
import com.sos.JSHelper.Options.SOSOptionUserName;

public interface ISOSDataProviderOptions {

    // @SuppressWarnings("unused") private static final String conSVNVersion =
    // "$Id$";

    public abstract SOSOptionUrl geturl(); // public String geturl

    public abstract ISOSDataProviderOptions seturl(SOSOptionUrl pstrValue); // public
                                                                            // SOSConnection2OptionsSuperClass
                                                                            // seturl

    public abstract String getinclude(); // public String getinclude

    public abstract ISOSDataProviderOptions setinclude(String pstrValue); // public
                                                                          // SOSFtpOptionsSuperClass
                                                                          // setinclude

    public abstract SOSOptionBoolean getuse_zlib_compression(); // public String
                                                                // getuse_zlib_compression

    public abstract ISOSDataProviderOptions setuse_zlib_compression(SOSOptionBoolean pstrValue); // public
                                                                                                 // SOSConnection2OptionsSuperClass
                                                                                                 // setuse_zlib_compression

    public abstract SOSOptionInteger getzlib_compression_level(); // public
                                                                  // String
                                                                  // getzlib_compression_level

    public abstract ISOSDataProviderOptions setzlib_compression_level(SOSOptionInteger pstrValue); // public
                                                                                                   // SOSConnection2OptionsSuperClass
                                                                                                   // setzlib_compression_level

    public abstract String getProtocolCommandListener(); // public String
                                                         // getProtocolCommandListener

    public abstract ISOSDataProviderOptions setProtocolCommandListener(String pstrValue); // public
                                                                                          // SOSConnection2OptionsSuperClass
                                                                                          // setProtocolCommandListener

    /** \brief getaccount
     *
     * \details
     *
     * \return
     *
     * @return */
    public abstract SOSOptionString getaccount();

    /** \brief setaccount
     *
     * \details
     *
     * \return
     *
     * @param p_account */
    public abstract void setaccount(SOSOptionString p_account);

    public abstract String getmake_Dirs(); // public String getmake_Dirs

    public abstract ISOSDataProviderOptions setmake_Dirs(String pstrValue); // public
                                                                            // SOSFtpOptionsSuperClass
                                                                            // setmake_Dirs

    public abstract String getplatform(); // public String getplatform

    public abstract ISOSDataProviderOptions setplatform(String pstrValue); // public
                                                                           // SOSFtpOptionsSuperClass
                                                                           // setplatform

    /** \brief getreplacement
     *
     * \details
     *
     * \return
     *
     * @return */
    public abstract SOSOptionString getreplacement();

    /** \brief setreplacement
     *
     * \details
     *
     * \return
     *
     * @param p_replacement */
    public abstract void setreplacement(SOSOptionString p_replacement);

    /** \brief getreplacing
     *
     * \details
     *
     * \return
     *
     * @return */
    public abstract SOSOptionRegExp getreplacing();

    /** \brief setreplacing
     *
     * \details
     *
     * \return
     *
     * @param p_replacing */
    public abstract void setreplacing(SOSOptionRegExp p_replacing);

    public abstract SOSOptionBoolean getstrict_hostKey_checking(); // public
                                                                   // SOSOptionBoolean
                                                                   // getStrict_HostKey_Checking

    public abstract void setstrict_hostKey_checking(String pstrValue); // public
                                                                       // SOSFtpOptionsSuperClass
                                                                       // setStrict_HostKey_Checking

    public abstract SOSOptionString getTFN_Post_Command(); // public String
                                                           // getTFN_Post_Command

    public abstract ISOSDataProviderOptions setTFN_Post_Command(SOSOptionString pstrValue); // public
                                                                                            // SOSConnection2OptionsSuperClass
                                                                                            // setTFN_Post_Command

    public abstract String getPost_Command(); // public String getPost_Command

    public abstract ISOSDataProviderOptions setPost_Command(String pstrValue); // public
                                                                               // SOSFTPOptions
                                                                               // setPost_Command

    public abstract String getPre_Command(); // public String getPre_Command

    public abstract ISOSDataProviderOptions setPre_Command(String pstrValue); // public
                                                                              // SOSFTPOptions
                                                                              // setPre_Command

    public abstract String getFtpS_protocol(); // public String getFtpS_protocol

    public abstract ISOSDataProviderOptions setFtpS_protocol(String pstrValue); // public
                                                                                // SOSConnection2OptionsAlternate
                                                                                // setFtpS_protocol

    public abstract String getloadClassName(); // public String getloadClassName

    public abstract ISOSDataProviderOptions setloadClassName(String pstrValue); // public
                                                                                // SOSConnection2OptionsSuperClass
                                                                                // setloadClassName

    /** \brief getjavaClassPath :
     *
     * \details
     *
     *
     * \return */
    public abstract SOSOptionString getjavaClassPath();

    /** \brief setjavaClassPath :
     *
     * \details
     *
     *
     * @param javaClassPath : */
    public abstract void setjavaClassPath(SOSOptionString p_javaClassPath);

    /** \brief gethost : Host-Name This parameter specifies th
     *
     * \details This parameter specifies the hostname or IP address of the
     * server to which a connection has to be made.
     *
     * \return Host-Name This parameter specifies th */
    public abstract SOSOptionHostName getHost();

    /** \brief sethost : Host-Name This parameter specifies th
     *
     * \details This parameter specifies the hostname or IP address of the
     * server to which a connection has to be made.
     *
     * @param host : Host-Name This parameter specifies th */
    public abstract void setHost(SOSOptionHostName p_host);

    /** \brief getpassive_mode : passive_mode Passive mode for FTP is often used
     * wit
     *
     * \details Passive mode for FTP is often used with firewalls. Valid values
     * are 0 or 1.
     *
     * \return passive_mode Passive mode for FTP is often used wit */
    public abstract SOSOptionBoolean getpassive_mode();

    /** \brief setpassive_mode : passive_mode Passive mode for FTP is often used
     * wit
     *
     * \details Passive mode for FTP is often used with firewalls. Valid values
     * are 0 or 1.
     *
     * @param passive_mode : passive_mode Passive mode for FTP is often used wit */
    public abstract void setpassive_mode(SOSOptionBoolean p_passive_mode);

    /** \brief getport : Port-Number to be used for Data-Transfer
     *
     * \details Port by which files should be transferred. For FTP this is
     * usually port 21, for SFTP this is usually port 22.
     *
     * \return Port-Number to be used for Data-Transfer */
    public abstract SOSOptionPortNumber getport();

    /** \brief setport : Port-Number to be used for Data-Transfer
     *
     * \details Port by which files should be transferred. For FTP this is
     * usually port 21, for SFTP this is usually port 22.
     *
     * @param port : Port-Number to be used for Data-Transfer */
    public abstract void setport(SOSOptionPortNumber p_port);

    /** \brief getprotocol : Type of requested Datatransfer The values ftp, sftp
     *
     * \details The values ftp, sftp or ftps are valid for this parameter. If
     * sftp is used, then the ssh_* parameters will be applied.
     *
     * \return Type of requested Datatransfer The values ftp, sftp */
    public abstract SOSOptionTransferType getprotocol();

    /** \brief setprotocol : Type of requested Datatransfer The values ftp, sftp
     *
     * \details The values ftp, sftp or ftps are valid for this parameter. If
     * sftp is used, then the ssh_* parameters will be applied.
     *
     * @param protocol : Type of requested Datatransfer The values ftp, sftp */
    public abstract void setprotocol(SOSOptionTransferType p_protocol);

    /** \brief gettransfer_mode : Type of Character-Encoding Transfe
     *
     * \details Transfer mode is used for FTP exclusively and can be either
     * ascii or binary.
     *
     * \return Type of Character-Encoding Transfe */
    public abstract SOSOptionTransferMode gettransfer_mode();

    /** \brief settransfer_mode : Type of Character-Encoding Transfe
     *
     * \details Transfer mode is used for FTP exclusively and can be either
     * ascii or binary.
     *
     * @param transfer_mode : Type of Character-Encoding Transfe */
    public abstract void settransfer_mode(SOSOptionTransferMode p_transfer_mode);

    /** \brief getuser
     *
     * \details
     *
     * \return
     *
     * @return */
    public abstract SOSOptionUserName getUser();

    /** \brief getpassword
     *
     * \details
     *
     * \return
     *
     * @return */
    public abstract SOSOptionPassword getPassword();

    /** \brief setpassword
     *
     * \details
     *
     * \return
     *
     * @param p_password */
    public abstract void setPassword(SOSOptionPassword p_password);

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
    public abstract void setAllOptions(HashMap<String, String> pobjJSSettings) throws Exception; // public
                                                                                                 // void
                                                                                                 // setAllOptions
                                                                                                 // (HashMap
                                                                                                 // <String,
                                                                                                 // String>
                                                                                                 // JSSettings)

    /** \brief getssh_auth_file
     *
     * \details
     *
     * \return
     *
     * @return */
    public abstract SOSOptionInFileName getAuth_file();

    /** \brief setssh_auth_file
     *
     * \details
     *
     * \return
     *
     * @param p_ssh_auth_file */
    public abstract void setAuth_file(SOSOptionInFileName p_ssh_auth_file);

    /** \brief getssh_auth_method
     *
     * \details
     *
     * \return
     *
     * @return */
    public abstract SOSOptionAuthenticationMethod getAuth_method();

    /** \brief setssh_auth_method
     *
     * \details
     *
     * \return
     *
     * @param p_ssh_auth_method */
    public abstract void setAuth_method(SOSOptionAuthenticationMethod p_ssh_auth_method);

    /** \brief setUser
     *
     * \details
     *
     * \return
     *
     * @param pobjUser */
    public abstract void setUser(SOSOptionUserName pobjUser);

    /** \brief getdomain
     *
     * \details
     *
     * \return
     *
     * @return */
    public abstract SOSOptionString getdomain();

    /** \brief setdomain
     *
     * \details
     *
     * \return
     *
     * @param p_domain */
    public abstract void setdomain(SOSOptionString p_domain);
}