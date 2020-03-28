package com.sos.vfs.ftp.common;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.JSHelper.Options.SOSOptionProxyProtocol.Protocol;

public class SOSFTPSProxySelector extends ProxySelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFTPSProxySelector.class);

    private Protocol proxyProtocol;
    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;

    public SOSFTPSProxySelector(Protocol protocol, String host, int port, String user, String password) {
        proxyProtocol = protocol;
        proxyHost = host;
        proxyPort = port;
        proxyUser = user;
        proxyPassword = password;

        LOGGER.info(String.format("using proxy. protocol = %s, host = %s:%s, user = %s ", proxyProtocol, proxyHost, proxyPort, proxyUser));
    }

    @Override
    public List<Proxy> select(URI uri) {
        Proxy proxy = null;
        List<Proxy> result = null;

        LOGGER.debug(String.format("using proxy [%s]. protocol = %s, host = %s:%s, user = %s ", uri.getScheme(), proxyProtocol, proxyHost, proxyPort,
                proxyUser));

        if (!SOSString.isEmpty(proxyUser)) {
            Authenticator.setDefault(new Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    PasswordAuthentication p = new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                    return p;
                }
            });
        }
        if (proxyProtocol.equals(Protocol.http)) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        } else if (proxyProtocol.equals(Protocol.socks4) || proxyProtocol.equals(Protocol.socks5)) {
            proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
        }

        if (proxy != null) {
            result = new ArrayList<Proxy>();
            result.add(proxy);
        }

        return result;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress addr, IOException ex) {
        if (uri == null || addr == null || ex == null) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }
        throw new UnsupportedOperationException(String.format("connect failed[uri: %s][socket address: %s]: %s", uri.toString(), addr.toString(), ex
                .toString()));
    }

}
