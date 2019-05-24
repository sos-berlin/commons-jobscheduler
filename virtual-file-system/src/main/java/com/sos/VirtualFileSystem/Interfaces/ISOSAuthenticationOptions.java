package com.sos.VirtualFileSystem.Interfaces;

import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionUserName;

public interface ISOSAuthenticationOptions {

    public abstract SOSOptionInFileName getAuthFile();

    public abstract void setAuthFile(SOSOptionInFileName authFile);

    public abstract void setUseKeyAgent(SOSOptionBoolean keyAgent);

    public abstract SOSOptionBoolean isUseKeyAgent();

    public abstract SOSOptionAuthenticationMethod getAuthMethod();

    public abstract void setAuthMethod(SOSOptionAuthenticationMethod authMethod);

    public abstract SOSOptionPassword getPassword();

    public abstract void setPassword(SOSOptionPassword password);

    public abstract SOSOptionPassword getPassphrase();

    public abstract void setPassphrase(SOSOptionPassword passphrase);

    public abstract SOSOptionUserName getUser();

    public abstract void setUser(SOSOptionUserName user);

}