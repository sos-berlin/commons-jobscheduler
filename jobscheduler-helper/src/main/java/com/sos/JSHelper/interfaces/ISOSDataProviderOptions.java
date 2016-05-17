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

    public abstract SOSOptionUrl getUrl();

    public abstract ISOSDataProviderOptions setUrl(SOSOptionUrl pstrValue);

    public abstract String getInclude();

    public abstract ISOSDataProviderOptions setInclude(String pstrValue);

    public abstract SOSOptionBoolean getUseZlibCompression();

    public abstract ISOSDataProviderOptions setUseZlibCompression(SOSOptionBoolean pstrValue);

    public abstract SOSOptionInteger getZlibCompressionLevel();

    public abstract ISOSDataProviderOptions setZlibCompressionLevel(SOSOptionInteger pstrValue);

    public abstract String getProtocolCommandListener();

    public abstract ISOSDataProviderOptions setProtocolCommandListener(String pstrValue);

    public abstract SOSOptionString getAccount();

    public abstract void setAccount(SOSOptionString pAccount);

    public abstract String getMakeDirs();

    public abstract ISOSDataProviderOptions setMakeDirs(String pstrValue);

    public abstract String getPlatform();

    public abstract ISOSDataProviderOptions setPlatform(String pstrValue);

    public abstract SOSOptionString getReplacement();

    public abstract void setReplacement(SOSOptionString pReplacement);

    public abstract SOSOptionRegExp getReplacing();

    public abstract void setReplacing(SOSOptionRegExp pReplacing);

    public abstract SOSOptionBoolean getStrictHostKeyChecking();

    public abstract void setStrictHostKeyChecking(String pstrValue);

    public abstract SOSOptionString getTfnPostCommand();

    public abstract ISOSDataProviderOptions setTfnPostCommand(SOSOptionString pstrValue);

    public abstract String getPostCommand();

    public abstract ISOSDataProviderOptions setPostCommand(String pstrValue);

    public abstract String getPreCommand();

    public abstract ISOSDataProviderOptions setPreCommand(String pstrValue);

    public abstract String getFtpsProtocol();

    public abstract ISOSDataProviderOptions setFtpsProtocol(String pstrValue);

    public abstract String getLoadClassName();

    public abstract ISOSDataProviderOptions setLoadClassName(String pstrValue);

    public abstract SOSOptionString getJavaClassPath();

    public abstract void setJavaClassPath(SOSOptionString pJavaClassPath);

    public abstract SOSOptionHostName getHost();

    public abstract void setHost(SOSOptionHostName pHost);

    public abstract SOSOptionBoolean getPassiveMode();

    public abstract void setPassiveMode(SOSOptionBoolean pPassiveMode);

    public abstract SOSOptionPortNumber getPort();

    public abstract void setPort(SOSOptionPortNumber pPort);

    public abstract SOSOptionTransferType getProtocol();

    public abstract void setProtocol(SOSOptionTransferType pProtocol);

    public abstract SOSOptionTransferMode getTransferMode();

    public abstract void setTransferMode(SOSOptionTransferMode pTransferMode);

    public abstract SOSOptionUserName getUser();

    public abstract SOSOptionPassword getPassword();

    public abstract void setPassword(SOSOptionPassword pPassword);

    public abstract void setAllOptions(HashMap<String, String> pobjJSSettings) throws Exception;

    public abstract SOSOptionInFileName getAuthFile();

    public abstract void setAuthFile(SOSOptionInFileName pSshAuthFile);

    public abstract SOSOptionAuthenticationMethod getAuthMethod();

    public abstract void setAuthMethod(SOSOptionAuthenticationMethod pSshAuthMethod);

    public abstract void setUser(SOSOptionUserName pobjUser);

    public abstract SOSOptionString getDomain();

    public abstract void setDomain(SOSOptionString pDomain);

}