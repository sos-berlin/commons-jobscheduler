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

    public abstract SOSOptionUrl geturl();

    public abstract ISOSDataProviderOptions seturl(SOSOptionUrl pstrValue);

    public abstract String getinclude();

    public abstract ISOSDataProviderOptions setinclude(String pstrValue);

    public abstract SOSOptionBoolean getuse_zlib_compression();

    public abstract ISOSDataProviderOptions setuse_zlib_compression(SOSOptionBoolean pstrValue);

    public abstract SOSOptionInteger getzlib_compression_level();

    public abstract ISOSDataProviderOptions setzlib_compression_level(SOSOptionInteger pstrValue);

    public abstract String getProtocolCommandListener();

    public abstract ISOSDataProviderOptions setProtocolCommandListener(String pstrValue);

    public abstract SOSOptionString getaccount();

    public abstract void setaccount(SOSOptionString p_account);

    public abstract String getmake_Dirs();

    public abstract ISOSDataProviderOptions setmake_Dirs(String pstrValue);

    public abstract String getplatform();

    public abstract ISOSDataProviderOptions setplatform(String pstrValue);

    public abstract SOSOptionString getreplacement();

    public abstract void setreplacement(SOSOptionString p_replacement);

    public abstract SOSOptionRegExp getreplacing();

    public abstract void setreplacing(SOSOptionRegExp p_replacing);

    public abstract SOSOptionBoolean getstrict_hostKey_checking();

    public abstract void setstrict_hostKey_checking(String pstrValue);

    public abstract SOSOptionString getTFN_Post_Command();

    public abstract ISOSDataProviderOptions setTFN_Post_Command(SOSOptionString pstrValue);

    public abstract String getPost_Command();

    public abstract ISOSDataProviderOptions setPost_Command(String pstrValue);

    public abstract String getPre_Command();

    public abstract ISOSDataProviderOptions setPre_Command(String pstrValue);

    public abstract String getFtpS_protocol();

    public abstract ISOSDataProviderOptions setFtpS_protocol(String pstrValue);

    public abstract String getloadClassName();

    public abstract ISOSDataProviderOptions setloadClassName(String pstrValue);

    public abstract SOSOptionString getjavaClassPath();

    public abstract void setjavaClassPath(SOSOptionString p_javaClassPath);

    public abstract SOSOptionHostName getHost();

    public abstract void setHost(SOSOptionHostName p_host);

    public abstract SOSOptionBoolean getpassive_mode();

    public abstract void setpassive_mode(SOSOptionBoolean p_passive_mode);

    public abstract SOSOptionPortNumber getport();

    public abstract void setport(SOSOptionPortNumber p_port);

    public abstract SOSOptionTransferType getprotocol();

    public abstract void setprotocol(SOSOptionTransferType p_protocol);

    public abstract SOSOptionTransferMode gettransfer_mode();

    public abstract void settransfer_mode(SOSOptionTransferMode p_transfer_mode);

    public abstract SOSOptionUserName getUser();

    public abstract SOSOptionPassword getPassword();

    public abstract void setPassword(SOSOptionPassword p_password);

    public abstract void setAllOptions(HashMap<String, String> pobjJSSettings) throws Exception;

    public abstract SOSOptionInFileName getAuth_file();

    public abstract void setAuth_file(SOSOptionInFileName p_ssh_auth_file);

    public abstract SOSOptionAuthenticationMethod getAuth_method();

    public abstract void setAuth_method(SOSOptionAuthenticationMethod p_ssh_auth_method);

    public abstract void setUser(SOSOptionUserName pobjUser);

    public abstract SOSOptionString getdomain();

    public abstract void setdomain(SOSOptionString p_domain);

}