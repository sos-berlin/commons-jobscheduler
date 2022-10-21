package com.sos.vfs.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.ssl.TrustMaterial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSFileEntry.EntryType;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.http.common.SOSHTTPRequestEntity;

import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSHTTP extends SOSCommonProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHTTP.class);
    private MultiThreadedHttpConnectionManager connectionManager;
    private HttpClient httpClient;
    private HttpURL rootUrl = null;
    private String configuredRootUrl = null;
    private HashMap<String, Long> fileSizes = null;
    private HashMap<String, PutMethod> putMethods = null;
    private LinkedHashMap<String, String> headers;

    private String proxyHost = null;
    private int proxyPort = 0;
    private String proxyUser = null;
    private String proxyPassword = null;
    private int lastStatusCode = -1;

    private boolean raiseJobSchedulerException = true;
    private GetMethod lastInputStreamGetMethod = null;

    public SOSHTTP() {
        super();
        fileSizes = new HashMap<String, Long>();
        putMethods = new HashMap<String, PutMethod>();
    }

    @Override
    public boolean isConnected() {
        return httpClient != null && connectionManager != null;
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        super.connect(options);
        proxyHost = getProviderOptions().proxyHost.getValue();
        proxyPort = getProviderOptions().proxyPort.value();
        proxyUser = getProviderOptions().proxyUser.getValue();
        proxyPassword = getProviderOptions().proxyPassword.getValue();
        doConnect();
        doLogin(getProviderOptions().user.getValue(), getProviderOptions().password.getValue());
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

    private String normalizeHttpPath(String path) {
        if (!path.toLowerCase().startsWith("http://") && !path.toLowerCase().startsWith("https://")) {
            if (path.startsWith("http:/")) {
                return path.replace("http:/", "http://");
            } else if (path.startsWith("https:/")) {
                return path.replace("https:/", "https://");
            }
            String root = configuredRootUrl;
            if (!root.endsWith("/")) {
                root = root + "/";
            }
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            path = root + path;
        }
        return path;
    }

    @Override
    public ISOSProviderFile getFile(String fileName) {
        ISOSProviderFile file = new SOSHTTPFile(fileName);
        file.setProvider(this);
        return file;
    }

    @Override
    public boolean fileExists(final String path) {
        GetMethod method = new GetMethod(normalizeHttpPath(path));
        setHttpMethodHeaders(method);
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

    @Override
    public boolean directoryExists(final String path) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]directoryExists", path));
        }
        return isDirectory(path);
    }

    private void doLogin(final String user, final String password) throws Exception {
        LOGGER.debug(SOSVfs_D_132.params(user));
        if (!SOSString.isEmpty(user)) {
            Credentials credentials = new UsernamePasswordCredentials(user, password);
            httpClient.getState().setCredentials(AuthScope.ANY, credentials);
        }
        checkConnection();
    }

    private void checkConnection() throws Exception {
        String uri = rootUrl.getURI();
        GetMethod method = new GetMethod(uri);
        setHttpMethodHeaders(method);
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

    private void doConnect() {
        if (!this.isConnected()) {
            try {
                HostConfiguration hc = new HostConfiguration();
                if (host.toLowerCase().startsWith("https://") || host.toLowerCase().startsWith("http://")) {
                    URL url = new URL(host);
                    if (url.getPort() != -1) {
                        port = url.getPort();
                    }
                    configuredRootUrl = host;
                    host = url.getHost();
                    String _rootUrl = url.getProtocol() + "://" + host + ":" + port + url.getPath();
                    if (!url.getPath().endsWith("/")) {
                        _rootUrl += "/";
                    }
                    if ("https".equalsIgnoreCase(url.getProtocol())) {
                        rootUrl = new HttpsURL(_rootUrl);
                        StrictSSLProtocolSocketFactory psf = new StrictSSLProtocolSocketFactory();
                        psf.setCheckHostname(getProviderOptions().verifyCertificateHostname.value());
                        if (!psf.getCheckHostname()) {
                            LOGGER.info(
                                    "*********************** Security warning *********************************************************************");
                            LOGGER.info("Jade option \"verify_certificate_hostname\" is currently \"false\". ");
                            LOGGER.info(
                                    "The certificate verification process will not verify the DNS name of the certificate presented by the server,");
                            LOGGER.info("with the hostname of the server in the URL used by the Yade client.");
                            LOGGER.info(
                                    "**************************************************************************************************************");
                        }
                        if (getProviderOptions().acceptUntrustedCertificate.value()) {
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
                    rootUrl = new HttpURL(host, port, "/");
                    configuredRootUrl = rootUrl.getURI();
                    hc.setHost(new HttpHost(host, port));
                }
                if (!configuredRootUrl.endsWith("/")) {
                    configuredRootUrl = configuredRootUrl + "/";
                }
                LOGGER.info(SOSVfs_D_0101.params(host, port));
                connectionManager = new MultiThreadedHttpConnectionManager();
                httpClient = new HttpClient(connectionManager);
                httpClient.setHostConfiguration(hc);
                setHeaders();
                setProxyCredentionals();
                logReply();
            } catch (Exception ex) {
                throw new JobSchedulerException(ex);
            }
        } else {
            LOGGER.warn(SOSVfs_D_0103.params(host, port));
        }
    }

    private void setHeaders() throws IOException {
        if (!SOSString.isEmpty(getProviderOptions().http_headers.getValue())) {
            headers = readHeaders(getProviderOptions().http_headers.getValue());
            LOGGER.info(String.format("[HTTPHeaders]%s", StringUtils.stripEnd(StringUtils.stripStart(headers.toString(), "{"), "}")));
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
            setHttpMethodHeaders(m);
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
            setHttpMethodHeaders(m);
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
            setHttpMethodHeaders(m);
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

    private ByteArrayOutputStream getOutputStream4Append(String path) throws Exception {
        // @TODO original Name
        path = path.replace(getBaseOptions().atomicPrefix.getValue(), "");
        path = path.replace(getBaseOptions().atomicSuffix.getValue(), "");

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
                byte[] buffer = new byte[getBaseOptions().bufferSize.value()];
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
    public OutputStream getOutputStream(final String path, boolean append, boolean resume) {
        String uri = normalizeHttpPath(path);
        try {
            PutMethod m = new PutMethod(uri);
            SOSHTTPRequestEntity re = null;
            if (append) {
                re = new SOSHTTPRequestEntity(getOutputStream4Append(path));
            } else {
                re = new SOSHTTPRequestEntity();
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
        setHttpMethodHeaders(m);
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
    public void mkdir(final String pathname) throws IOException {
        // LOGGER.info("not implemented yet");
    }

    @Override
    public long size(final String path) throws Exception {
        if (fileSizes.containsKey(path)) {
            return fileSizes.get(path);
        }
        Long size = new Long(-1);
        String uri = normalizeHttpPath(path);
        GetMethod method = new GetMethod(uri);
        setHttpMethodHeaders(method);
        try {
            httpClient.executeMethod(method);
            int sc = method.getStatusCode();
            boolean success = isSuccessStatusCode(sc);
            if (!success) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(getHttpMethodExceptionText(method, uri));
                }
                if (sc != 404) {// because of DisableErrorOnNoFilesFound=true
                    throw new Exception(getHttpMethodExceptionText(method, uri));
                }
            }
            if (success) {
                size = method.getResponseContentLength();
                if (size < 0) {
                    size = getInputStreamLen(method.getResponseBodyAsStream());
                }
            }
        } catch (Exception ex) {
            reply = ex.toString();
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
    public SOSFileEntry getFileEntry(String path) throws Exception {
        reply = "get OK";

        path = normalizeHttpPath(path);
        long size = size(path);
        if (size < 0) {
            return null;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s]found", path));
        }

        SOSFileEntry entry = new SOSFileEntry(EntryType.HTTP);
        entry.setDirectory(false);
        String fileName = getBaseNameFromPath(path);
        entry.setFilename(fileName);
        // e.g. for HTTP(s) transfers with the file names like SET-217?filter=13400
        entry.setNormalizedFilename(URLEncoder.encode(fileName, "UTF-8"));
        entry.setFilesize(size);
        entry.setParentPath(getFullParentFromPath(path));

        return entry;
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
            setHttpMethodHeaders(lastInputStreamGetMethod);
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

    private void setHttpMethodHeaders(HttpMethod method) {
        if (headers != null && headers.size() > 0) {
            headers.forEach((k, v) -> {
                method.setRequestHeader(k.toString(), v.toString());
            });
        }
    }

    private LinkedHashMap<String, String> readHeaders(final String val) throws IOException {
        final LinkedHashMap<String, String> m = new LinkedHashMap<>();
        // see JadeXml2IniConverter DELIMITER_MERGED_CHILDS_HEADERS
        Stream.of(val.split("\\|")).forEach(e -> {
            String arrS = e.trim();
            String[] arr = arrS.split(":");
            if (arr.length > 0) {
                String k = arr[0].trim();
                if (k.length() > 0) {
                    m.put(k, arr.length > 1 ? arr[1].trim() : "");
                }
            }
        });
        return m;
    }

    private int getMethodStatusCode(HttpMethod method) {
        try {
            return method.getStatusCode();
        } catch (Exception ex) {
            //
        }
        return -1;
    }

    private String getMethodStatusText(HttpMethod method) {
        try {
            return method.getStatusText();
        } catch (Exception ex) {
            //
        }
        return "";
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
    public boolean isHTTP() {
        return true;
    }

}