package com.sos.VirtualFileSystem.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.contrib.ssl.StrictSSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.TrustMaterial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSDestinationOptions;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSFileEntry.EntryType;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsHTTP extends SOSVfsTransferBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsHTTP.class);
    private MultiThreadedHttpConnectionManager connectionManager;
    private HttpClient httpClient;
    private HttpURL rootUrl = null;
    private HashMap<String, Long> fileSizes = null;
    private HashMap<String, PutMethod> putMethods = null;
    private String proxyHost = null;
    private int proxyPort = 0;
    private String proxyUser = null;
    private String proxyPassword = null;
    private boolean simulateShell = false;
    private int lastStatusCode = -1;
    private boolean raiseJobSchedulerException = true;
    private GetMethod lastInputStreamGetMethod = null;

    public SOSVfsHTTP() {
        super();
        fileSizes = new HashMap<String, Long>();
        putMethods = new HashMap<String, PutMethod>();
    }

    @Override
    public ISOSConnection connect() throws Exception {
        connect(destinationOptions);
        return this;
    }

    @Override
    public ISOSConnection connect(final SOSDestinationOptions options) throws Exception {
        destinationOptions = options;
        if (destinationOptions == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("connection2OptionsAlternate"));
        }
        proxyHost = destinationOptions.proxyHost.getValue();
        proxyPort = destinationOptions.proxyPort.value();
        proxyUser = destinationOptions.proxyUser.getValue();
        proxyPassword = destinationOptions.proxyPassword.getValue();
        doConnect(destinationOptions.host.getValue(), destinationOptions.port.value());
        return this;
    }

    @Override
    public ISOSConnection authenticate(final ISOSAuthenticationOptions options) {
        authenticationOptions = options;
        try {
            doAuthenticate(authenticationOptions);
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
            doLogin(user, password);
            reply = "OK";
            LOGGER.info(SOSVfs_D_133.params(userName));
            logReply();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("authentication"), e);
        }
    }

    @Override
    public void disconnect() {
        reply = "disconnect OK";
        fileSizes = new HashMap<String, Long>();
        putMethods = new HashMap<String, PutMethod>();
        lastStatusCode = -1;
        if (connectionManager != null) {
            try {
                connectionManager.shutdown();
            } catch (Exception ex) {
                reply = "disconnect: " + ex;
            }
            connectionManager = null;
            httpClient = null;
        }
        LOGGER.info(reply);
    }

    @Override
    public boolean isConnected() {
        return httpClient != null && connectionManager != null;
    }

    private String normalizeHttpPath(String path) {
        if (!path.toLowerCase().startsWith("http://") && !path.toLowerCase().startsWith("https://")) {
            if (path.startsWith("http:/")) {
                return path.replace("http:/", "http://");
            } else if (path.startsWith("https:/")) {
                return path.replace("https:/", "https://");
            }
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            path = httpClient.getHostConfiguration().getHostURL() + path;
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
            throw new JobSchedulerException(SOSVfs_E_184.params("getFile", remoteFile, localFile), ex);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (Exception e) {
                //
            }
            resetLastInputStreamGetMethod();
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
            httpClient.executeMethod(method);
            return isSuccessStatusCode(method.getStatusCode());
        } catch (Exception ex) {
        } finally {
            try {
                method.releaseConnection();
            } catch (Exception ex) {
                //
            }
        }
        return false;
    }

    private void doLogin(final String user, final String password) throws Exception {
        this.userName = user;
        LOGGER.debug(SOSVfs_D_132.params(userName));
        if (!SOSString.isEmpty(userName)) {
            Credentials credentials = new UsernamePasswordCredentials(userName, password);
            httpClient.getState().setCredentials(AuthScope.ANY, credentials);
        }
        checkConnection();
    }

    private void checkConnection() throws Exception {
        String uri = rootUrl.getURI();
        GetMethod method = new GetMethod(uri);
        try {
            if (isServerErrorStatusCode(httpClient.executeMethod(method))) {
                throw new Exception(getHttpMethodExceptionText(method, uri));
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                method.releaseConnection();
            } catch (Exception ex) {
                //
            }
        }
    }

    private ISOSConnection doAuthenticate(final ISOSAuthenticationOptions options) throws Exception {
        authenticationOptions = options;
        doLogin(authenticationOptions.getUser().getValue(), authenticationOptions.getPassword().getValue());
        return this;
    }

    private void doConnect(final String phost, final int pport) {
        if (!this.isConnected()) {
            try {
                port = pport;
                host = phost;
                HostConfiguration hc = new HostConfiguration();
                if (phost.toLowerCase().startsWith("https://") || phost.toLowerCase().startsWith("http://")) {
                    URL url = new URL(phost);
                    port = (url.getPort() == -1) ? url.getDefaultPort() : url.getPort();
                    host = url.getHost();
                    String _rootUrl = url.getProtocol() + "://" + host + ":" + port + url.getPath();
                    if (!url.getPath().endsWith("/")) {
                        _rootUrl += "/";
                    }
                    if ("https".equalsIgnoreCase(url.getProtocol())) {
                        rootUrl = new HttpsURL(_rootUrl);
                        StrictSSLProtocolSocketFactory psf = new StrictSSLProtocolSocketFactory();
                        psf.setCheckHostname(destinationOptions.verifyCertificateHostname.value());
                        if (!psf.getCheckHostname()) {
                            LOGGER.info(
                                    "*********************** Security warning *********************************************************************");
                            LOGGER.info("Jade option \"verify_certificate_hostname\" is currently \"false\". ");
                            LOGGER.info(
                                    "The certificate verification process will not verify the DNS name of the certificate presented by the server,");
                            LOGGER.info("with the hostname of the server in the URL used by the Jade client.");
                            LOGGER.info(
                                    "**************************************************************************************************************");
                        }
                        if (destinationOptions.acceptUntrustedCertificate.value()) {
                            psf.useDefaultJavaCiphers();
                            psf.addTrustMaterial(TrustMaterial.TRUST_ALL);
                        }
                        Protocol p = new Protocol("https", (ProtocolSocketFactory) psf, port);
                        Protocol.registerProtocol("https", p);
                        hc.setHost(host, port, p);
                    } else {
                        rootUrl = new HttpURL(_rootUrl);
                        hc.setHost(new HttpHost(host, port));
                    }
                } else {
                    rootUrl = new HttpURL(phost, pport, "/");
                    hc.setHost(new HttpHost(host, port));
                }
                LOGGER.info(SOSVfs_D_0101.params(host, port));
                connectionManager = new MultiThreadedHttpConnectionManager();
                httpClient = new HttpClient(connectionManager);
                httpClient.setHostConfiguration(hc);
                setProxyCredentionals();
                logReply();
            } catch (Exception ex) {
                throw new JobSchedulerException(ex);
            }
        } else {
            LOGGER.warn(SOSVfs_D_0103.params(host, port));
        }
    }

    private void setProxyCredentionals() throws Exception {
        if (!SOSString.isEmpty(proxyHost)) {
            LOGGER.info(String.format("using proxy: host = %s, port = %s, user = %s, pass = ?", proxyHost, proxyPort, proxyUser));
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
            Credentials credentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);
            AuthScope authScope = new AuthScope(proxyHost, proxyPort);
            httpClient.getState().setProxyCredentials(authScope, credentials);
        }
    }

    private boolean isSuccessStatusCode(int statusCode) {
        lastStatusCode = statusCode;
        if (statusCode >= 200 && statusCode < 300) {
            return true;
        }
        return false;
    }

    private boolean isServerErrorStatusCode(int statusCode) {
        lastStatusCode = statusCode;
        if (statusCode >= 500) {
            return true;
        }
        return false;
    }

    @Override
    public void rename(String from, String to) {
        from = normalizeHttpPath(from);
        to = normalizeHttpPath(to);

        PutMethod m = null;
        InputStream is = null;
        try {
            m = new PutMethod(to);
            is = getInputStream(from);
            m.setRequestEntity(new InputStreamRequestEntity(is));
            httpClient.executeMethod(m);
            try {
                is.close();
                is = null;
            } catch (Exception ex) {
            }
            if (!isSuccessStatusCode(m.getStatusCode())) {
                throw new Exception(getHttpMethodExceptionText(m, to));
            }
            delete(from, false);
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_188.params("rename", from, to), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            resetLastInputStreamGetMethod();
            if (m != null) {
                try {
                    m.releaseConnection();
                } catch (Exception e) {
                }
            }
        }
        reply = "mv OK";
        LOGGER.info(getHostID(SOSVfs_I_189.params(from, to, getReplyString())));
    }

    @Override
    public void delete(String path, boolean checkIsDirectory) {
        String uri = normalizeHttpPath(path);
        DeleteMethod m = null;

        try {
            if (checkIsDirectory && this.isDirectory(uri)) {
                throw new JobSchedulerException(SOSVfs_E_186.params(uri));
            }
            m = new DeleteMethod(uri);
            httpClient.executeMethod(m);
            if (!isSuccessStatusCode(m.getStatusCode())) {
                throw new Exception(getHttpMethodExceptionText(m, uri));
            }
        } catch (Exception ex) {
            reply = ex.toString();
            throw new JobSchedulerException(SOSVfs_E_187.params("delete", uri), ex);
        } finally {
            if (m != null) {
                try {
                    m.releaseConnection();
                } catch (Exception e) {
                }
            }
        }
        reply = "rm OK";
        LOGGER.info(getHostID(SOSVfs_D_181.params("delete", uri, getReplyString())));
    }

    @Override
    public boolean isDirectory(final String path) {
        String uri = normalizeHttpPath(path);
        TraceMethod m = null;
        try {
            uri = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
            m = new TraceMethod(uri);
            httpClient.executeMethod(m);
            if (m.getStatusCode() <= 400) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        } finally {
            if (m != null) {
                try {
                    m.releaseConnection();
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public OutputStream getOutputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    private ByteArrayOutputStream getOutputStream4Append(String path) throws Exception {
        // @TODO original Name
        path = path.replace(getOptions().atomicPrefix.getValue(), "");
        path = path.replace(getOptions().atomicSuffix.getValue(), "");

        InputStream source = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            raiseJobSchedulerException = false;
            source = getInputStream(path);
        } catch (RuntimeException ex) {
            raiseJobSchedulerException = true;
            if (lastStatusCode == 404) {
                return out;
            } else {
                throw new JobSchedulerException(SOSVfs_E_193.params("getInputStream()", path), ex);
            }
        } finally {
            raiseJobSchedulerException = true;
        }

        try {
            if (source != null) {
                byte[] buffer = new byte[getOptions().bufferSize.value()];
                int bytesTransferred;
                while ((bytesTransferred = source.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesTransferred);
                }
                source.close();
                source = null;

                out.flush();
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (source != null) {
                source.close();
            }
            resetLastInputStreamGetMethod();
        }

        return out;
    }

    @Override
    public OutputStream getOutputStream(final String path) {
        String uri = normalizeHttpPath(path);
        try {
            PutMethod m = new PutMethod(uri);
            SOSVfsHTTPRequestEntity re = null;
            if (getOptions().appendFiles.value()) {
                re = new SOSVfsHTTPRequestEntity(getOutputStream4Append(path));
            } else {
                re = new SOSVfsHTTPRequestEntity();
            }
            m.setRequestEntity(re);

            putMethods.put(uri, m);
            return re.getOutputStream();
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getOutputStream()", uri), ex);
        }
    }

    protected void put(String path) throws Exception {
        String uri = normalizeHttpPath(path);

        PutMethod m = putMethods.get(uri);
        if (m == null) {
            throw new Exception(String.format("[%s]PutMethod is null", uri));
        }

        try {
            httpClient.executeMethod(m);
            if (!isSuccessStatusCode(m.getStatusCode())) {
                throw new Exception(getHttpMethodExceptionText(m, uri));
            }
        } catch (Exception e) {
            throw e;
        } finally {
            putMethods.remove(uri);

            try {
                m.releaseConnection();
            } catch (Exception e) {
            }
        }

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
                //
            }
        }
        return new Long(total);
    }

    @Override
    public long size(final String path) throws Exception {
        if (fileSizes.containsKey(path)) {
            return fileSizes.get(path);
        }
        Long size = new Long(-1);
        String uri = normalizeHttpPath(path);
        GetMethod method = new GetMethod(uri);
        try {
            httpClient.executeMethod(method);
            if (!isSuccessStatusCode(method.getStatusCode())) {
                throw new Exception(this.getHttpMethodExceptionText(method, uri));
            }
            size = method.getResponseContentLength();
            if (size < 0) {
                size = getInputStreamLen(method.getResponseBodyAsStream());
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            fileSizes.put(path, size);
            try {
                method.releaseConnection();
            } catch (Exception ex) {
                //
            }
        }
        return size;
    }

    @Override
    public boolean changeWorkingDirectory(String path) {
        return true;
    }

    @Override
    public SOSFileEntry getFileEntry(String pathname) throws Exception {
        reply = "get OK";
        return new SOSFileEntry(EntryType.HTTP);// TODO
    }

    @Override
    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException {
        if (path.isEmpty()) {
            path = "/";
        }
        reply = "ls OK";
        return new ArrayList<SOSFileEntry>();// TODO
    }

    @Override
    public InputStream getInputStream(final String fileName) {
        try {
            String uri = normalizeHttpPath(fileName);
            lastInputStreamGetMethod = new GetMethod(uri);
            httpClient.executeMethod(lastInputStreamGetMethod);
            if (!isSuccessStatusCode(lastInputStreamGetMethod.getStatusCode())) {
                throw new Exception(getHttpMethodExceptionText(lastInputStreamGetMethod, uri));
            }
            return lastInputStreamGetMethod.getResponseBodyAsStream();
        } catch (Exception ex) {
            if (raiseJobSchedulerException) {
                throw new JobSchedulerException(SOSVfs_E_193.params("getInputStream()", fileName), ex);
            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    private String getHttpMethodExceptionText(HttpMethod method, String uri) throws Exception {
        return getHttpMethodExceptionText(method, uri, null);
    }

    private String getHttpMethodExceptionText(HttpMethod method, String uri, Exception ex) throws Exception {
        int code = getMethodStatusCode(method);
        String text = getMethodStatusText(method);
        if (ex == null) {
            return String.format("HTTP %s[%s][%s] = %s", method.getName(), uri, code, text);
        } else {
            return String.format("HTTP %s[%s][%s][%s] = %s", method.getName(), uri, code, text, ex);
        }
    }

    private int getMethodStatusCode(HttpMethod method) {
        int val = -1;
        try {
            val = method.getStatusCode();
        } catch (Exception ex) {
            //
        }
        return val;
    }

    private String getMethodStatusText(HttpMethod method) {
        String val = "";
        try {
            val = method.getStatusText();
        } catch (Exception ex) {
            //
        }
        return val;
    }

    public void resetLastInputStreamGetMethod() {
        if (lastInputStreamGetMethod != null) {
            try {
                lastInputStreamGetMethod.releaseConnection();
            } catch (Exception ex) {
            }
            lastInputStreamGetMethod = null;
        }
    }

    @Override
    public boolean isSimulateShell() {
        return simulateShell;
    }

    @Override
    public void setSimulateShell(boolean val) {
        simulateShell = val;
    }
}