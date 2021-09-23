package com.sos.vfs.sftp.sshj.common.proxy.http;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.net.DefaultSocketFactory;

import com.sos.vfs.sftp.sshj.common.proxy.Proxy;

public class HttpProxySocketFactory extends DefaultSocketFactory {

    private final Proxy proxy;

    public HttpProxySocketFactory(final Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public Socket createSocket() throws UnknownHostException, IOException {
        return new HttpProxySocket(proxy);
    }

}
