package com.sos.vfs.http.common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import com.sos.vfs.common.SOSProxySelector;

public class SOSHTTPClient {

    private CloseableHttpClient client = null;
    private final URI baseUri;
    private final String user; // test, MAIN\test
    private final String password;

    public SOSHTTPClient(URI baseUri) {
        this(baseUri, null, null);
    }

    public SOSHTTPClient(URI baseUri, String user, String password) {
        this.baseUri = baseUri;
        this.user = user;
        this.password = password;
    }

    public void create() throws Exception {
        create(null, null);
    }

    public void create(SOSProxySelector proxy) throws Exception {
        create(proxy, null);
    }

    public void create(SOSHTTPClientSSL ssl) throws Exception {
        create(null, ssl);
    }

    public void create(SOSProxySelector proxy, SOSHTTPClientSSL ssl) throws Exception {
        if (baseUri == null) {
            return;
        }
        close();
        HttpClientBuilder builder = HttpClientBuilder.create();
        CredentialsProvider provider = new BasicCredentialsProvider();
        if (user != null && password != null) {
            String domain = null;
            String userName = null;
            String[] arr = user.split("\\\\");
            if (arr.length > 1) {
                domain = arr[0];
                userName = arr[1];
            } else {
                userName = user;
            }
            // UsernamePasswordCredentials ???
            provider.setCredentials(AuthScope.ANY, (Credentials) new NTCredentials(userName, password, getWorkstation(), domain));
        }
        if (proxy != null) {
            provider.setCredentials(proxy.getAuthScope(), proxy.getCredentials());
            builder.setRoutePlanner(new SystemDefaultRoutePlanner(new DefaultSchemePortResolver(), proxy));
        }
        builder.setDefaultCredentialsProvider(provider);

        if (baseUri.getScheme().equalsIgnoreCase("https")) {
            if (ssl != null) {
                builder.setSSLContext(ssl.getSSLContext());
                builder.setSSLHostnameVerifier(ssl.getHostnameVerifier());
            }
        }
        // builder.setDefaultRequestConfig(RequestConfig.custom().setExpectContinueEnabled(true).build());
        client = builder.build();
    }

    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {

            }
        }
    }

    public CloseableHttpClient getClient() {
        return client;
    }

    private String getWorkstation() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Throwable e) {
            return "unknown";
        }
    }

}
