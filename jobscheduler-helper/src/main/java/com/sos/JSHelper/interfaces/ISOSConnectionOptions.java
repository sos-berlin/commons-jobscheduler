package com.sos.JSHelper.interfaces;

import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionUserName;

/** @author KB */
public interface ISOSConnectionOptions {

    public SOSOptionHostName getHost();

    public void setAlternativeHost(SOSOptionHostName pAlternativeHost);

    public SOSOptionHostName getAlternativeHost();

    public void setAlternativePassword(SOSOptionPassword pAlternativePassword);

    public SOSOptionPassword getAlternativePassword();

    public SOSOptionString getAlternativeAccount();

    public SOSOptionString getAlternativePassiveMode();

    public SOSOptionPortNumber getAlternativePort();

    public abstract void setHost(SOSOptionHostName host);

    public abstract SOSOptionPortNumber getPort();

    public abstract void setPort(SOSOptionPortNumber port);

    public abstract SOSOptionString getProxyHost();

    public abstract void setProxyHost(SOSOptionString proxyHost);

    public abstract SOSOptionPortNumber getProxyPort();

    public abstract void setProxyPort(SOSOptionPortNumber proxyPort);

    public abstract void setProxyUser(SOSOptionUserName proxyUser);

    public abstract SOSOptionUserName getProxyUser();

    public abstract void setProxyPassword(SOSOptionPassword proxyPassword);

    public abstract SOSOptionPassword getProxyPassword();

    public abstract SOSOptionUserName getUser();

    public SOSOptionBoolean getRaiseExceptionOnError();

}