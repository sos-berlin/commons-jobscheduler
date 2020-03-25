package com.sos.VirtualFileSystem.Interfaces;

import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.VirtualFileSystem.Options.SOSDestinationOptions;

public interface ISOSConnection {

    public ISOSConnection connect() throws Exception;

    public ISOSConnection connect(SOSDestinationOptions options) throws Exception;

    public ISOSConnection connect(ISOSDataProviderOptions options) throws Exception;

    @Deprecated
    public ISOSConnection connect(ISOSConnectionOptions options) throws Exception;

    public ISOSConnection connect(final String host, final int port) throws Exception;

    public ISOSConnection authenticate(ISOSAuthenticationOptions options) throws Exception;

    public void closeConnection() throws Exception;

}
