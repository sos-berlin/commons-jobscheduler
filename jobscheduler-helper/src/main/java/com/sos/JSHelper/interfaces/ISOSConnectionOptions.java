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

    public void setalternative_host(SOSOptionHostName p_alternative_host);

    public SOSOptionHostName getalternative_host();

    public void setalternative_password(SOSOptionPassword p_alternative_password);

    public SOSOptionPassword getalternative_password();

    public SOSOptionString getalternative_account();

    public SOSOptionString getalternative_passive_mode();

    public SOSOptionPortNumber getalternative_port();

    public abstract void setHost(SOSOptionHostName host);

    public abstract SOSOptionPortNumber getPort();

    public abstract void setPort(SOSOptionPortNumber port);

    public abstract SOSOptionString getProxy_host();

    public abstract void setProxy_host(SOSOptionString proxyHost);

    public abstract SOSOptionPortNumber getProxy_port();

    public abstract void setProxy_port(SOSOptionPortNumber proxyPort);

    public abstract void setProxy_user(SOSOptionUserName proxyUser);

    public abstract SOSOptionUserName getProxy_user();

    public abstract void setProxy_password(SOSOptionPassword proxyPassword);

    public abstract SOSOptionPassword getProxy_password();

    public abstract SOSOptionUserName getUser();

    public SOSOptionBoolean getraise_exception_on_error();

}