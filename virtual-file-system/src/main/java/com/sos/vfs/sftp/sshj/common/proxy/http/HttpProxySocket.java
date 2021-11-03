package com.sos.vfs.sftp.sshj.common.proxy.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Base64;

import org.apache.commons.io.IOUtils;

import com.sos.vfs.sftp.sshj.common.proxy.Proxy;

import sos.util.SOSString;

public class HttpProxySocket extends Socket {

    private final Proxy proxy;

    public HttpProxySocket(final Proxy proxy) throws UnknownHostException, IOException {
        super();
        this.proxy = proxy;
    }

    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        super.connect(proxy.getProxy().address(), proxy.getConnectTimeout());

        String basicAuth = null;
        if (!SOSString.isEmpty(proxy.getUser())) {
            basicAuth = new String(Base64.getEncoder().encode(new String(proxy.getUser() + ":" + proxy.getPassword()).getBytes()));
        }

        InetSocketAddress address = (InetSocketAddress) endpoint;
        OutputStream out = this.getOutputStream();
        IOUtils.write(String.format("CONNECT %s:%s HTTP/1.0\r\n", address.getHostName(), address.getPort()), out, proxy.getCharset());
        if (basicAuth != null) {
            IOUtils.write("Proxy-Authorization: Basic ", out, proxy.getCharset());
            IOUtils.write(basicAuth, out, proxy.getCharset());
        }
        IOUtils.write("\r\n", out, proxy.getCharset());
        IOUtils.write("\r\n", out, proxy.getCharset());
        out.flush();

        InputStream in = this.getInputStream();
        String response = new LineNumberReader(new InputStreamReader(in)).readLine();
        if (response == null) {
            throw new SocketException(String.format("[%s]missing response", ((InetSocketAddress) proxy.getProxy().address()).getHostName()));
        }
        if (response.contains("200")) {
            in.skip(in.available());
        } else {
            throw new SocketException(String.format("[%s][invalid response]%s", ((InetSocketAddress) proxy.getProxy().address()).getHostName(),
                    response));
        }
    }
}
