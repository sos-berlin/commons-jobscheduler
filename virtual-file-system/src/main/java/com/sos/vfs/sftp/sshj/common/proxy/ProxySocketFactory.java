package com.sos.vfs.sftp.sshj.common.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.net.DefaultSocketFactory;

import com.sos.vfs.sftp.sshj.common.proxy.http.HttpProxySocketFactory;

public class ProxySocketFactory extends DefaultSocketFactory {

    private final Proxy proxy;

    public ProxySocketFactory(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public Socket createSocket() throws IOException {
        switch (proxy.getProxy().type()) {
        case HTTP:
            return new HttpProxySocketFactory(proxy).createSocket();
        case SOCKS:
            return new DefaultSocketFactory(proxy.getProxy()).createSocket();
        default:
            break;
        }
        return null;
    }

    @Override
    public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
        return createSocket();
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
        return createSocket();
    }

    @Override
    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException {
        return createSocket();
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
        return createSocket();
    }
}
