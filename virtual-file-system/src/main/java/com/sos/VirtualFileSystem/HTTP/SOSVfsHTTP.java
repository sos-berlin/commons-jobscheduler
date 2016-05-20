package com.sos.VirtualFileSystem.HTTP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.contrib.ssl.StrictSSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.TrustMaterial;
import org.apache.log4j.Logger;

import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author Robert Ehrlich */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsHTTP extends SOSVfsTransferBaseClass {

    private static final Logger LOGGER = Logger.getLogger(SOSVfsHTTP.class);
    private MultiThreadedHttpConnectionManager connectionManager;
    private HttpClient httpClient;
    private HttpURL rootUrl = null;
    private HashMap<String, Long> fileSizes = null;
    private String proxyHost = null;
    private int proxyPort = 0;
    private String proxyUser = null;
    private String proxyPassword = null;
    private boolean simulateShell = false;

    public SOSVfsHTTP() {
        super();
        this.fileSizes = new HashMap<String, Long>();
    }

    @Override
    public ISOSConnection connect() throws Exception {
        this.connect(this.connection2OptionsAlternate);
        return this;
    }

    @Override
    public ISOSConnection connect(final SOSConnection2OptionsAlternate options) throws Exception {
        connection2OptionsAlternate = options;
        if (connection2OptionsAlternate == null) {
            raiseException(SOSVfs_E_190.params("connection2OptionsAlternate"));
        }
        proxyHost = connection2OptionsAlternate.proxyHost.getValue();
        proxyPort = connection2OptionsAlternate.proxyPort.value();
        proxyUser = connection2OptionsAlternate.proxyUser.getValue();
        proxyPassword = connection2OptionsAlternate.proxyPassword.getValue();
        this.doConnect(connection2OptionsAlternate.host.getValue(), connection2OptionsAlternate.port.value());
        return this;
    }

    @Override
    public ISOSConnection authenticate(final ISOSAuthenticationOptions options) {
        authenticationOptions = options;
        try {
            this.doAuthenticate(authenticationOptions);
        } catch (JobSchedulerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
        }
        return this;
    }

    @Override
    public void login(final String user, final String password) {
        try {
            this.doLogin(user, password);
            reply = "OK";
            LOGGER.info(SOSVfs_D_133.params(userName));
            this.logReply();
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_134.params("authentication"));
        }
    }

    @Override
    public void disconnect() {
        reply = "disconnect OK";
        this.fileSizes = new HashMap<String, Long>();
        if (this.connectionManager != null) {
            try {
                this.connectionManager.shutdown();
            } catch (Exception ex) {
                reply = "disconnect: " + ex;
            }
            this.connectionManager = null;
            this.httpClient = null;
        }
        LOGGER.info(reply);
    }

    @Override
    public boolean isConnected() {
        return httpClient != null && connectionManager != null;
    }

    private String normalizeHttpPath(String path) {
        if (!path.toLowerCase().startsWith("http://") && !path.toLowerCase().startsWith("https://")) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            path = this.httpClient.getHostConfiguration().getHostURL() + path;
        }
        return path;
    }

    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) {
        long fileSize = -1;
        FileOutputStream outputStream = null;
        try {
            InputStream responseStream = getInputStream(remoteFile);
            File local = new File(localFile);
            outputStream = new FileOutputStream(local, append);
            byte buffer[] = new byte[1000];
            int numOfBytes = 0;
            while ((numOfBytes = responseStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, numOfBytes);
            }
            fileSize = local.length();
            reply = "get OK";
            LOGGER.info(getHostID(SOSVfs_I_182.params("getFile", remoteFile, localFile, getReplyString())));
        } catch (Exception ex) {
            reply = ex.toString();
            raiseException(ex, SOSVfs_E_184.params("getFile", remoteFile, localFile));
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (Exception e) {
            }
        }
        return fileSize;
    }

    @Override
    public ISOSVirtualFile getFileHandle(String fileName) {
        fileName = adjustFileSeparator(fileName);
        ISOSVirtualFile file = new SOSVfsHTTPFile(fileName);
        file.setHandler(this);
        return file;
    }

    @Override
    protected boolean fileExists(final String path) {
        GetMethod method = new GetMethod(normalizeHttpPath(path));
        try {
            this.httpClient.executeMethod(method);
            return isSuccessStatusCode(method.getStatusCode());
        } catch (Exception ex) {
        } finally {
            try {
                method.releaseConnection();
            } catch (Exception ex) {
            }
        }
        return false;
    }

    private void doLogin(final String user, final String password) throws Exception {
        this.userName = user;
        LOGGER.debug(SOSVfs_D_132.params(userName));
        if (!SOSString.isEmpty(userName)) {
            Credentials credentials = new UsernamePasswordCredentials(this.userName, password);
            this.httpClient.getState().setCredentials(AuthScope.ANY, credentials);
        }
        this.checkConnection();
    }

    private void checkConnection() throws Exception {
        String uri = this.rootUrl.getURI();
        GetMethod method = new GetMethod(uri);
        try {
            if (isServerErrorStatusCode(this.httpClient.executeMethod(method))) {
                throw new Exception(this.getHttpMethodExceptionText(method, uri));
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                method.releaseConnection();
            } catch (Exception ex) {
            }
        }
    }

    private ISOSConnection doAuthenticate(final ISOSAuthenticationOptions options) throws Exception {
        authenticationOptions = options;
        this.doLogin(authenticationOptions.getUser().getValue(), authenticationOptions.getPassword().getValue());
        return this;
    }

    private void doConnect(final String phost, final int pport) {
        if (!this.isConnected()) {
            try {
                this.port = pport;
                this.host = phost;
                HostConfiguration hc = new HostConfiguration();
                if (phost.toLowerCase().startsWith("https://") || phost.toLowerCase().startsWith("http://")) {
                    URL url = new URL(phost);
                    this.port = (url.getPort() == -1) ? url.getDefaultPort() : url.getPort();
                    this.host = url.getHost();
                    String _rootUrl = url.getProtocol() + "://" + this.host + ":" + this.port + url.getPath();
                    if (!url.getPath().endsWith("/")) {
                        _rootUrl += "/";
                    }
                    if ("https".equalsIgnoreCase(url.getProtocol())) {
                        this.rootUrl = new HttpsURL(_rootUrl);
                        StrictSSLProtocolSocketFactory psf = new StrictSSLProtocolSocketFactory();
                        psf.setCheckHostname(connection2OptionsAlternate.verifyCertificateHostname.value());
                        if (!psf.getCheckHostname()) {
                            LOGGER.info("*********************** Security warning *********************************************************************");
                            LOGGER.info("Jade option \"verify_certificate_hostname\" is currently \"false\". ");
                            LOGGER.info("The certificate verification process will not verify the DNS name of the certificate presented by the server,");
                            LOGGER.info("with the hostname of the server in the URL used by the Jade client.");
                            LOGGER.info("**************************************************************************************************************");
                        }
                        if (connection2OptionsAlternate.acceptUntrustedCertificate.value()) {
                            psf.useDefaultJavaCiphers();
                            psf.addTrustMaterial(TrustMaterial.TRUST_ALL);
                        }
                        Protocol p = new Protocol("https", (ProtocolSocketFactory) psf, this.port);
                        Protocol.registerProtocol("https", p);
                        hc.setHost(this.host, this.port, p);
                    } else {
                        this.rootUrl = new HttpURL(_rootUrl);
                        hc.setHost(new HttpHost(host, port));
                    }
                } else {
                    this.rootUrl = new HttpURL(phost, pport, "/");
                    hc.setHost(new HttpHost(host, port));
                }
                LOGGER.info(SOSVfs_D_0101.params(host, port));
                connectionManager = new MultiThreadedHttpConnectionManager();
                httpClient = new HttpClient(connectionManager);
                httpClient.setHostConfiguration(hc);
                this.setProxyCredentionals();
                this.logReply();
            } catch (Exception ex) {
                throw new JobSchedulerException(ex);
            }
        } else {
            logWARN(SOSVfs_D_0103.params(host, port));
        }
    }

    private void setProxyCredentionals() throws Exception {
        if (!SOSString.isEmpty(this.proxyHost)) {
            LOGGER.info(String.format("using proxy: host = %s, port = %s, user = %s, pass = ?", this.proxyHost, this.proxyPort, this.proxyUser));
            httpClient.getHostConfiguration().setProxy(this.proxyHost, this.proxyPort);
            Credentials credentials = new UsernamePasswordCredentials(this.proxyUser, this.proxyPassword);
            AuthScope authScope = new AuthScope(this.proxyHost, this.proxyPort);
            this.httpClient.getState().setProxyCredentials(authScope, credentials);
        }
    }

    private boolean isSuccessStatusCode(int statusCode) {
        if (statusCode == HttpStatus.SC_OK) {
            return true;
        }
        return false;
    }

    private boolean isServerErrorStatusCode(int statusCode) {
        if (statusCode >= 500) {
            return true;
        }
        return false;
    }

    @Override
    public OutputStream getOutputStream() {
        // TO DO Auto-generated method stub
        return null;
    }

    public static byte[] getBytesX(InputStream is) throws IOException {
        int len;
        int size = 1024;
        byte[] buf;
        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1) {
                bos.write(buf, 0, len);
            }
            buf = bos.toByteArray();
        }
        return buf;
    }

    public static Long getInputStreamLen(InputStream is) throws IOException {
        long total = 0;
        try {
            int intBytesTransferred = 0;
            byte[] buffer = new byte[1024];
            while ((intBytesTransferred = is.read(buffer)) != -1) {
                total += intBytesTransferred;
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }
        return new Long(total);
    }

    @Override
    public long size(final String path) throws Exception {
        if (this.fileSizes.containsKey(path)) {
            return this.fileSizes.get(path);
        }
        Long size = new Long(-1);
        String uri = normalizeHttpPath(path);
        GetMethod method = new GetMethod(uri);
        try {
            this.httpClient.executeMethod(method);
            if (!isSuccessStatusCode(method.getStatusCode())) {
                throw new Exception(this.getHttpMethodExceptionText(method, uri));
            }
            size = method.getResponseContentLength();
            if (size < 0) {
                size = getInputStreamLen(method.getResponseBodyAsStream());
            }
            this.fileSizes.put(path, size);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                method.releaseConnection();
            } catch (Exception ex) {
            }
        }
        return size;
    }

    @Override
    public boolean changeWorkingDirectory(String path) {
        return true;
    }

    @Override
    public String[] listNames(String path) throws IOException {
        if (path.isEmpty()) {
            path = "/";
        }
        reply = "ls OK";
        return new String[] { path };
    }

    @Override
    public InputStream getInputStream(final String fileName) {
        try {
            String uri = normalizeHttpPath(fileName);
            GetMethod method = new GetMethod(uri);
            this.httpClient.executeMethod(method);
            if (!isSuccessStatusCode(method.getStatusCode())) {
                throw new Exception(this.getHttpMethodExceptionText(method, uri));
            }
            return method.getResponseBodyAsStream();
        } catch (Exception ex) {
            raiseException(ex, SOSVfs_E_193.params("getInputStream()", fileName));
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream(final String fileName) {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getInputStream() {
        // TO DO Auto-generated method stub
        return null;
    }

    private String getHttpMethodExceptionText(HttpMethod method, String uri) throws Exception {
        return this.getHttpMethodExceptionText(method, uri, null);
    }

    private String getHttpMethodExceptionText(HttpMethod method, String uri, Exception ex) throws Exception {
        int code = this.getMethodStatusCode(method);
        String text = this.getMethodStatusText(method);
        if (ex == null) {
            return String.format("HTTP [%s][%s] = %s", uri, code, text);
        } else {
            return String.format("HTTP [%s][%s][%s] = %s", uri, code, text, ex);
        }
    }

    private int getMethodStatusCode(HttpMethod method) {
        int val = -1;
        try {
            val = method.getStatusCode();
        } catch (Exception ex) {
        }
        return val;
    }

    private String getMethodStatusText(HttpMethod method) {
        String val = "";
        try {
            val = method.getStatusText();
        } catch (Exception ex) {
        }
        return val;
    }

    @Override
    public SOSFileEntries getSOSFileEntries() {
        return sosFileEntries;
    }

    @Override
    public boolean isSimulateShell() {
        return this.simulateShell;
    }

    @Override
    public void setSimulateShell(boolean simulateShell) {
        this.simulateShell = simulateShell;
    }

}