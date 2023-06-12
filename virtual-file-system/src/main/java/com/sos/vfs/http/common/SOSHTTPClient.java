package com.sos.vfs.http.common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.vfs.common.SOSProxySelector;
import com.sos.vfs.common.options.SOSProviderOptions;

import sos.util.SOSKeyStoreReader;
import sos.util.SOSString;

public class SOSHTTPClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHTTPClient.class);

    private CloseableHttpClient client = null;
    private HttpClientContext context;
    private final String user; // test, MAIN\test
    private final String password;

    private URI baseURI;
    private String baseURIPath;
    private boolean isHTTPS;

    public SOSHTTPClient(final SOSProviderOptions options, boolean ntCredentials) throws Exception {
        this.baseURI = getBaseURI(options);
        this.baseURIPath = baseURI.getPath();
        this.user = normalizeValue(options.user.getValue());
        this.password = normalizeValue(options.password.getValue());
        create(getProxySelector(options), getSSL(options), ntCredentials);
    }

    private void create(SOSProxySelector proxy, SOSHTTPClientSSL ssl, boolean ntCredentials) throws Exception {
        if (baseURI == null) {
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

            if (ntCredentials) {
                provider.setCredentials(AuthScope.ANY, new NTCredentials(userName, password, getWorkstation(), domain));
            } else {
                // HttpHost targetHost = new HttpHost(baseURI.getHost());
                HttpHost targetHost = new HttpHost(baseURI.getHost(), baseURI.getPort(), baseURI.getScheme());
                provider.setCredentials(new AuthScope(targetHost), new UsernamePasswordCredentials(userName, password));

                AuthCache authCache = new BasicAuthCache();
                authCache.put(targetHost, new BasicScheme());

                // Add AuthCache to the execution context
                context = HttpClientContext.create();
                context.setCredentialsProvider(provider);
                context.setAuthCache(authCache);
            }
        }
        if (proxy != null) {
            provider.setCredentials(proxy.getAuthScope(), proxy.getCredentials());
            // builder.setRoutePlanner(new SystemDefaultRoutePlanner(new DefaultSchemePortResolver(), proxy));
            builder.setProxy(proxy.getHttpHost());
        }
        builder.setDefaultCredentialsProvider(provider);

        isHTTPS = false;
        if (baseURI.getScheme().equalsIgnoreCase("https")) {
            isHTTPS = true;
            if (ssl != null) {
                builder.setSSLContext(ssl.getSSLContext());
                builder.setSSLHostnameVerifier(ssl.getHostnameVerifier());
            }
        }
        builder.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build());
        client = builder.build();
    }

    public CloseableHttpResponse execute(HttpRequestBase request) throws Exception {
        // make sure request path is absolute
        // handleURI(request);
        // execute request and return response
        if (client == null) {
            throw new Exception("HTTPClient is null");
        }
        // setAuthHeader(request);
        // request.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 Firefox/26.0");
        return client.execute(request, context);
    }

    public void setBaseUriOnNotExists() {
        if (this.baseURI != null && this.baseURIPath != null) {
            try {
                String bu = this.baseURI.toString();
                this.baseURI = new URI(bu.substring(0, bu.indexOf(baseURIPath)) + "/");
                this.baseURIPath = baseURI.getPath();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[setBaseUriOnNotExists][old=%s][new=%s]baseURIPath=%s", bu, baseURI, baseURIPath));
                }
            } catch (URISyntaxException e) {
                LOGGER.error(String.format("[setBaseUriOnNotExists][%s]%s", this.baseURI, e.toString()), e);
            }
        }
    }

    private String normalizeValue(String val) {
        return SOSString.isEmpty(val) ? null : val;
    }

    public int getPort() {
        int p = baseURI.getPort();
        if (p == -1) {
            if (isHTTPS) {
                p = 443;
            } else {
                p = 80;
            }
        }
        return p;
    }

    private URI getBaseURI(final SOSProviderOptions options) throws URISyntaxException {
        StringBuilder sb = new StringBuilder();
        String hostParam = options.host.getValue();
        if (options.authMethod.isURL()) {
            sb.append(hostParam);
            if (!hostParam.endsWith("/")) {
                sb.append("/");
            }
        } else {
            if (hostParam.toLowerCase().startsWith("https://") || hostParam.toLowerCase().startsWith("http://")) {
                sb.append(hostParam);
                if (!hostParam.endsWith("/")) {
                    sb.append("/");
                }
            } else {
                sb.append("http://").append(hostParam);
                if (!SOSString.isEmpty(options.port.getValue())) {
                    sb.append(":").append(options.port.getValue());
                }
                sb.append("/");
            }
        }
        return new URI(sb.toString());
    }

    private SOSHTTPClientSSL getSSL(final SOSProviderOptions options) {
        SOSHTTPClientSSL ssl = null;
        if (baseURI.getScheme().equalsIgnoreCase("https")) {
            SOSKeyStoreReader ksr = getKeyStoreReader(options);
            ssl = new SOSHTTPClientSSL(ksr, ksr, options.verifyCertificateHostname.value(), options.acceptUntrustedCertificate.value());
            if (!ssl.getCheckHostname()) {
                LOGGER.info("*********************** Security warning *********************************************************************");
                LOGGER.info("Yade option \"verify_certificate_hostname\" is currently \"false\". ");
                LOGGER.info("The certificate verification process will not verify the DNS name of the certificate presented by the server,");
                LOGGER.info("with the hostname of the server in the URL used by the Yade client.");
                LOGGER.info("**************************************************************************************************************");
            }
        }
        return ssl;
    }

    private SOSKeyStoreReader getKeyStoreReader(final SOSProviderOptions options) {
        SOSKeyStoreReader ksr = null;
        if (!options.acceptUntrustedCertificate.value()) {
            String kf = options.keystoreFile.getValue();
            String kp = options.keystorePassword.getValue();
            String kt = options.keystoreType.getValue();

            Path path = SOSString.isEmpty(kf) ? null : Paths.get(kf);
            String password = SOSString.isEmpty(kp) ? null : kp;
            String type = SOSString.isEmpty(kt) ? null : kt;
            ksr = new SOSKeyStoreReader(SOSKeyStoreReader.Type.KeyTrustStore, path, password, type);
        }
        return ksr;
    }

    private SOSProxySelector getProxySelector(final SOSProviderOptions options) {
        SOSProxySelector selector = null;
        String host = options.proxyHost.getValue();
        if (!SOSString.isEmpty(host)) {
            int port = options.proxyPort.value();
            String user = options.proxyUser.getValue();
            String password = options.proxyPassword.getValue();
            selector = new SOSProxySelector(options.proxyProtocol.getValue(), host, port, SOSString.isEmpty(user) ? null : user, SOSString.isEmpty(
                    password) ? null : password);

            // only protocol=http is supported. socks protocol usage throws no error but the socks settings are completely ignored
            LOGGER.info(String.format("[using proxy]protocol=%s, host=%s:%s, user=%s", options.proxyProtocol.getValue(), host, port, user));
        }
        return selector;
    }

    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {

            }
        }
        context = null;
    }

    public CloseableHttpClient getClient() {
        return client;
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public String getBaseURIPath() {
        return baseURIPath;
    }

    public boolean isAbsolute(String fileName) {
        if (fileName == null) {
            return false;
        }
        String f = fileName.toLowerCase();
        return f.startsWith("https://") || f.startsWith("http://");
    }

    public URI normalizeURI(String rel) throws URISyntaxException {
        if (isAbsolute(rel)) {
            return new URI(rel).normalize();
        }
        return baseURI.resolve(new URI(rel)).normalize();
    }

    public HttpPropfind getConnectRequestMethod() throws IOException {
        return new HttpPropfind(baseURI, null, DavConstants.DEPTH_0);
    }

    public String getRelativeDirectoryPath(URI uri) {
        int bul = baseURI.toString().length();
        String u = uri.toString();
        String p = bul >= u.length() ? "/" : "/" + u.substring(bul);
        if (!p.equals("/")) {
            if (p.endsWith("/")) {// /transfer-1/
                // /transfer-1
                p = p.substring(0, p.length() - 1);
            } else {
                // /transfer-1/myfile.txt
                final int i = p.lastIndexOf("/");
                p = ((i >= 0) ? p.substring(0, i) : "");
                p = SOSString.isEmpty(p) ? "/" : p;
            }
        }
        return p;
    }

    private String getWorkstation() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Throwable e) {
            return "unknown";
        }
    }

}
