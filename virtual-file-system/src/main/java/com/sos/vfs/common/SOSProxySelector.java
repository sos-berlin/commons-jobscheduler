package com.sos.vfs.common;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

public class SOSProxySelector extends ProxySelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSProxySelector.class);

    private final String protocol;
    private final String host;
    private final int port;
    private final String user;
    private final String password;

    private List<Proxy> proxies;

    public SOSProxySelector(String protocol, String host, int port, String user, String password) {
        this.protocol = SOSString.isEmpty(protocol) ? "http" : protocol.toLowerCase();
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    @Override
    public List<Proxy> select(URI uri) {
        if (proxies != null) {
            return proxies;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[using proxy]protocol=%s, host=%s:%s, user=%s ", protocol, host, port, user));
        }

        Proxy proxy = null;
        if (protocol.equals("http")) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        } else if (protocol.startsWith("socks")) {// socks4,socks5
            proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
        }

        // setAuthenticator();
        proxies = proxy == null ? null : Collections.singletonList(proxy);
        return proxies;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress addr, IOException ex) {
        if (uri == null || addr == null || ex == null) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }
        throw new UnsupportedOperationException(String.format("connect failed[uri: %s][socket address: %s]: %s", uri.toString(), addr.toString(), ex
                .toString()));
    }

    public AuthScope getAuthScope() {
        return new AuthScope(host, port, AuthScope.ANY_REALM);
    }

    public Credentials getCredentials() {  // or NTCredentials???
        return new UsernamePasswordCredentials(user == null ? "" : user, password);
    }

    @SuppressWarnings("unused")
    private void setAuthenticator() {
        if (!SOSString.isEmpty(user)) {
            Authenticator.setDefault(new Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    PasswordAuthentication p = new PasswordAuthentication(user, password.toCharArray());
                    return p;
                }
            });
        }
    }

}
