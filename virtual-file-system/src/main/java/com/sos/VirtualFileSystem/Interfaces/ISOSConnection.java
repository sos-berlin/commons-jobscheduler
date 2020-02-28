package com.sos.VirtualFileSystem.Interfaces;

import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;

public interface ISOSConnection {

    public ISOSConnection connect() throws Exception;

    public ISOSConnection connect(SOSConnection2OptionsAlternate pobjConnectionOptions) throws Exception;

    public ISOSConnection connect(ISOSDataProviderOptions pobjConnectionOptions) throws Exception;

    @Deprecated
    public ISOSConnection connect(ISOSConnectionOptions pobjConnectionOptions) throws Exception;

    public ISOSConnection connect(final String pstrHostName, final int pintPortNumber) throws Exception;

    public ISOSConnection authenticate(ISOSAuthenticationOptions pobjAO) throws Exception;

    public void closeConnection() throws Exception;

}
