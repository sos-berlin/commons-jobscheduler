package com.sos.vfs.webdav.jackrabbit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.HttpCopy;
import org.apache.jackrabbit.webdav.client.methods.HttpDelete;
import org.apache.jackrabbit.webdav.client.methods.HttpMkcol;
import org.apache.jackrabbit.webdav.client.methods.HttpMove;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSFileEntry.EntryType;
import com.sos.vfs.common.SOSProxySelector;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.http.common.SOSHTTPClient;
import com.sos.vfs.http.common.SOSHTTPClientSSL;
import com.sos.vfs.webdav.common.ISOSWebDAV;
import com.sos.vfs.webdav.jackrabbit.common.SOSWebDAVInputStream;
import com.sos.vfs.webdav.jackrabbit.common.SOSWebDAVOutputStream;
import com.sos.vfs.webdav.jackrabbit.common.SOSWebDAVResource;

import sos.util.SOSDate;
import sos.util.SOSKeyStoreReader;
import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSWebDAV extends SOSCommonProvider implements ISOSWebDAV {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSWebDAV.class);

    private SOSHTTPClient client = null;
    private URI baseURI = null;
    private String baseURIPath = null;

    public SOSWebDAV() {
        super();
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        super.connect(options);

        baseURI = getBaseUri(options);

        String u = options.user.getValue();
        String p = options.password.getValue();
        client = new SOSHTTPClient(baseURI, SOSString.isEmpty(u) ? null : u, SOSString.isEmpty(p) ? null : p);
        client.create(getProxySelector(options), getSSL(options));

        URL baseURL = baseURI.toURL();
        host = baseURL.getHost();
        port = baseURL.getPort();
        baseURIPath = baseURL.getPath();

        HttpPropfind request = getConnectRequestMethod();
        try (CloseableHttpResponse response = execute(request)) {
            getMuiltiStatus(request, response);
        } catch (Throwable e) {
            throwException(request, e);
        }

        LOGGER.info(SOSVfs_D_0102.params(host, port));
        this.logReply();
    }

    private URI getBaseUri(final SOSProviderOptions options) throws URISyntaxException {
        StringBuilder sb = new StringBuilder();
        if (options.authMethod.isURL()) {
            sb.append(options.host.getValue());
            if (!options.host.getValue().endsWith("/")) {
                sb.append("/");
            }
        } else {
            sb.append("http://").append(options.host.getValue());
            if (!SOSString.isEmpty(options.port.getValue())) {
                sb.append(":").append(options.port.getValue());
            }
            sb.append("/");
        }
        return new URI(sb.toString());
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

    @Override
    public void disconnect() {
        reply = "disconnect OK";
        if (client != null) {
            try {
                client.close();
                client = null;
            } catch (Throwable ex) {
                reply = "disconnect: " + ex;
            }
        }
        LOGGER.info(reply);
    }

    @Override
    public boolean isConnected() {
        return client != null;
    }

    private URI normalize(String rel) throws URISyntaxException {
        return baseURI.resolve(new URI(rel)).normalize();

    }

    private CloseableHttpResponse execute(HttpRequestBase request) throws Exception {
        // make sure request path is absolute
        // handleURI(request);
        // execute request and return response
        if (client == null || client.getClient() == null) {
            throw new Exception("client is null");
        }
        return client.getClient().execute(request, (HttpClientContext) null);
    }

    @Override
    public void mkdir(final String path) {
        try {
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            if (isDirectory(path)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[mkdir][%s]already exists", path));
                }
                return;

            }
            SOSOptionFolderName folderName = new SOSOptionFolderName(path);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[mkdir][%s]try to create ...", path));
            }
            String[] subfolders = folderName.getSubFolderArrayReverse();
            int idx = subfolders.length;
            for (String folder : folderName.getSubFolderArrayReverse()) {
                if (isDirectory(folder)) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(SOSVfs_E_180.params(folder));
                    }
                    break;
                }
                idx--;
            }
            subfolders = folderName.getSubFolderArray();
            for (int i = idx; i < subfolders.length; i++) {
                mkSingleDir(subfolders[i]);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[mkdir][%s]created", subfolders[i]));
                }
            }
            reply = "mkdir OK";
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("[%s] mkdir failed", path), e);
        }
    }

    private void mkSingleDir(final String path) throws Exception {
        URI uri = normalize(path.endsWith("/") ? path : (path + "/"));
        try (CloseableHttpResponse response = execute(new HttpMkcol(uri))) {
            StatusLine statusLine = response.getStatusLine();
            int status = statusLine.getStatusCode();
            if (status / 100 != 2) {
                throw new Exception(String.format("[%s]%s", status, statusLine.getReasonPhrase()));
            }
        }
    }

    @Override
    public void rmdir(String path) {
        try {
            executeDeleteMethod(path.endsWith("/") ? path : (path + "/"));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[rmdir][%s]removed", path));
            }
            reply = "rmdir OK";
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("[%s] rmdir failed", path), e);
        }
    }

    private void executeDeleteMethod(final String path) throws Exception {
        URI uri = normalize(path);
        try (CloseableHttpResponse response = execute(new HttpDelete(uri))) {
            StatusLine statusLine = response.getStatusLine();
            int status = statusLine.getStatusCode();
            if (status / 100 != 2) {
                throw new Exception(String.format("[%s]%s", status, statusLine.getReasonPhrase()));
            }
        }
    }

    @Override
    public boolean isDirectory(final String path) {
        boolean isDirectory = false;
        try {
            // http://test.sos:9080/my_file.txt/
            URI uri = normalize(path.endsWith("/") ? path : (path + "/"));
            HttpPropfind request = getFileInfoRequestMethod(uri, DavConstants.DEPTH_0);

            try (CloseableHttpResponse response = execute(request)) {
                MultiStatus multiStatus = getMuiltiStatus(request, response);
                if (multiStatus != null) {
                    for (MultiStatusResponse msr : multiStatus.getResponses()) {
                        isDirectory = new SOSWebDAVResource(msr).isDirectory();
                    }
                }
            }
        } catch (Throwable e) {
            // LOGGER.error(e.toString(), e);
        }
        return isDirectory;
    }

    @Override
    public SOSFileEntry getFileEntry(String path) throws Exception {
        // http://test.sos:9080/my_file.txt
        URI uri = normalize(path);

        // /
        String parentPath = getRelativeDirectoryPath(uri);

        HttpPropfind request = getFileInfoRequestMethod(uri, DavConstants.DEPTH_0);
        SOSFileEntry entry = null;
        try (CloseableHttpResponse response = execute(request)) {
            MultiStatus multiStatus = getMuiltiStatus(request, response);
            if (multiStatus != null) {
                for (MultiStatusResponse msr : multiStatus.getResponses()) {
                    entry = getFileEntry(parentPath, new SOSWebDAVResource(msr));
                }
            }
        } catch (Throwable e) {
            throwException(request, e);
        }
        return entry;
    }

    private SOSFileEntry getFileEntry(String parentPath, SOSWebDAVResource r) {
        SOSFileEntry entry = new SOSFileEntry(EntryType.HTTP);
        entry.setDirectory(r.isDirectory());
        entry.setFilename(r.getName());
        entry.setFilesize(r.getSize());
        // entry.setLastModified(file.getGetLastModified());
        entry.setParentPath(parentPath);
        return entry;
    }

    private HttpPropfind getConnectRequestMethod() throws IOException {
        return new HttpPropfind(baseURI, null, DavConstants.DEPTH_0);
    }

    private HttpPropfind getFileInfoRequestMethod(URI uri, int depth) throws IOException {
        DavPropertyNameSet set = new DavPropertyNameSet();
        set.add(DavPropertyName.create(DavConstants.PROPERTY_RESOURCETYPE));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_GETCONTENTLENGTH));
        return new HttpPropfind(uri, set, depth);
    }

    private HttpPropfind getFileInfoLastModifiedRequestMethod(URI uri, int depth) throws IOException {
        DavPropertyNameSet set = new DavPropertyNameSet();
        set.add(DavPropertyName.create(DavConstants.PROPERTY_GETLASTMODIFIED));
        return new HttpPropfind(uri, set, depth);
    }

    private MultiStatus getMuiltiStatus(HttpPropfind request, CloseableHttpResponse response) throws DavException {
        request.checkSuccess(response);

        MultiStatus result = null;
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MULTI_STATUS) {
            result = request.getResponseBodyAsMultiStatus(response);
        }
        return result;
    }

    private void throwException(HttpPropfind request, Throwable e) throws Exception {
        if (e instanceof DavException) {
            DavException ex = (DavException) e;
            throw new Exception(String.format("[%s][%s]%s", request.getURI(), ex.getErrorCode(), ex.getStatusPhrase()), e);
        }
        throw new Exception(String.format("[%s]%s", request.getURI(), e.toString()), e);
    }

    private String getRelativeDirectoryPath(URI uri) {
        String p = "/" + uri.toString().substring(baseURI.toString().length());
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

    @Override
    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory) {
        try {
            List<SOSFileEntry> result = new ArrayList<>();
            if (path.isEmpty()) {
                path = "/";
            } else if (!path.endsWith("/")) {
                path = path + "/";
            }

            // http://test.sos:9080/transfer-1/
            URI uri = normalize(path);
            // /transfer-1
            String parentPath = getRelativeDirectoryPath(uri);

            HttpPropfind request = getFileInfoRequestMethod(uri, DavConstants.DEPTH_1);
            try (CloseableHttpResponse response = execute(request)) {
                MultiStatus multiStatus = getMuiltiStatus(request, response);
                if (multiStatus != null) {
                    int i = 0;
                    for (MultiStatusResponse msr : multiStatus.getResponses()) {
                        SOSWebDAVResource r = new SOSWebDAVResource(msr);
                        if (i == 0) {// the first entry is the path parameter (parent directory) ...
                            // if (checkIfExists && !(normalize(r.getName()).toString()).equals(currentDir)) {
                            // return result;
                            // }
                            if (checkIfIsDirectory && !r.isDirectory()) {
                                reply = "ls OK";
                                return result;
                            }
                            // ignore first entry
                        } else {
                            result.add(getFileEntry(parentPath, r));
                        }

                        i++;
                    }
                }

            } catch (Throwable e) {
                throwException(request, e);
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s][listNames] %s files or folders", path, result.size()));
            }
            reply = "ls OK";
            return result;
        } catch (Throwable e) {
            reply = e.toString();
            return null;
        }
    }

    @Override
    public long size(final String path) throws Exception {
        long size = -1;
        try {
            URI uri = normalize(path);
            HttpPropfind request = getFileInfoRequestMethod(uri, DavConstants.DEPTH_0);

            try (CloseableHttpResponse response = execute(request)) {
                MultiStatus multiStatus = getMuiltiStatus(request, response);
                if (multiStatus != null) {
                    for (MultiStatusResponse msr : multiStatus.getResponses()) {
                        size = new SOSWebDAVResource(msr).getSize();
                    }
                }
            }
        } catch (Throwable e) {
            // LOGGER.trace(e.getMessage());
        }
        return size;
    }

    @Override
    public void delete(String path, boolean checkIsDirectory) {
        try {
            if (checkIsDirectory && this.isDirectory(path)) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }
            executeDeleteMethod(path);
        } catch (Throwable ex) {
            reply = ex.toString();
            throw new JobSchedulerException(SOSVfs_E_187.params("delete", path), ex);
        }
        reply = "rm OK";
        LOGGER.info(getHostID(SOSVfs_D_181.params("delete", path, getReplyString())));
    }

    @Override
    public void rename(String from, String to) {
        try {
            try (CloseableHttpResponse response = execute(new HttpMove(normalize(from), normalize(to), true))) {
                StatusLine statusLine = response.getStatusLine();
                int status = statusLine.getStatusCode();
                if (status / 100 != 2) {
                    throw new Exception(String.format("[%s]%s", status, statusLine.getReasonPhrase()));
                }
            }
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_188.params("rename", from, to), e);
        }
        reply = "mv OK";
        LOGGER.info(getHostID(SOSVfs_I_189.params(from, to, getReplyString())));
    }

    @Override
    public InputStream getInputStream(String path) {

        try {
            URI uri = normalize(path);
            CloseableHttpResponse response = null;
            try {
                response = execute(new HttpGet(uri));

                StatusLine statusLine = response.getStatusLine();
                int status = statusLine.getStatusCode();
                if (status / 100 != 2) {
                    throw new Exception(String.format("[%s]%s", status, statusLine.getReasonPhrase()));
                }
                return new SOSWebDAVInputStream(response);
            } catch (Throwable e) {
                if (response != null) {
                    try {
                        response.close();
                    } catch (Throwable ee) {
                    }
                }
                throw e;
            }
        } catch (Throwable ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getInputStream()", path), ex);
        }
    }

    @Override
    public OutputStream getOutputStream(final String path, boolean append, boolean resume) {
        try {
            return new SOSWebDAVOutputStream(client.getClient(), normalize(path));
        } catch (Throwable ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getOutputStream()", path), ex);
        }
    }

    @Override
    public ISOSProviderFile getFile(String fileName) {
        try {
            fileName = adjustFileSeparator(fileName);
            if (!isAbsolute(fileName)) {
                fileName = (baseURIPath + fileName).replaceAll("//+", "/");
            }
            ISOSProviderFile file = new SOSWebDAVFile(fileName);
            file.setProvider(this);

            return file;
        } catch (Throwable e) {
            LOGGER.error(String.format("[getFile][%s]%s", fileName, e.toString()), e);
            return null;
        }
    }

    private boolean isAbsolute(String fileName) {
        return fileName.startsWith(baseURIPath);
    }

    @Override
    public String getModificationDateTime(final String path) {
        String result = null;
        try {
            // http://test.sos:9080/my_file.txt/
            URI uri = normalize(path);
            HttpPropfind request = getFileInfoLastModifiedRequestMethod(uri, DavConstants.DEPTH_0);

            try (CloseableHttpResponse response = execute(request)) {
                MultiStatus multiStatus = getMuiltiStatus(request, response);
                if (multiStatus != null) {
                    for (MultiStatusResponse msr : multiStatus.getResponses()) {
                        result = new SOSWebDAVResource(msr, true).getLastModifiedAsString();
                    }
                }
            }
        } catch (Throwable ex) {
            //
        }
        return result;
    }

    protected long getModificationTimeStamp(final String path) {
        long result = -1L;
        try {
            // http://test.sos:9080/my_file.txt/
            URI uri = normalize(path);
            HttpPropfind request = getFileInfoLastModifiedRequestMethod(uri, DavConstants.DEPTH_0);

            try (CloseableHttpResponse response = execute(request)) {
                MultiStatus multiStatus = getMuiltiStatus(request, response);
                if (multiStatus != null) {
                    for (MultiStatusResponse msr : multiStatus.getResponses()) {
                        result = new SOSWebDAVResource(msr, true).getLastModifiedAsLong();
                    }
                }
            }
        } catch (Throwable ex) {
            //
        }
        return result;
    }

    @Override
    public boolean fileExists(final String path) {
        boolean exists = false;
        try {
            // http://test.sos:9080/my_file.txt
            URI uri = normalize(path);
            HttpPropfind request = getFileInfoRequestMethod(uri, DavConstants.DEPTH_0);

            try (CloseableHttpResponse response = execute(request)) {
                MultiStatus multiStatus = getMuiltiStatus(request, response);
                if (multiStatus != null) {
                    for (MultiStatusResponse msr : multiStatus.getResponses()) {
                        exists = !(new SOSWebDAVResource(msr).isDirectory());
                    }
                }
            }
        } catch (Throwable e) {
            return false;
        }
        return exists;
    }

    @Override
    public boolean directoryExists(final String filename) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]directoryExists", filename));
        }
        return isDirectory(filename);
    }

    @Override
    public long copy(final String source, final String target) {
        try {
            Instant start = Instant.now();

            try (CloseableHttpResponse response = execute(new HttpCopy(normalize(source), normalize(target), true, false))) {
                StatusLine statusLine = response.getStatusLine();
                int status = statusLine.getStatusCode();
                if (status / 100 != 2) {
                    throw new Exception(String.format("[%s]%s", status, statusLine.getReasonPhrase()));
                }
            }
            Instant end = Instant.now();
            reply = new StringBuilder("copy OK (").append(SOSDate.getDuration(start, end)).append(")").toString();
            LOGGER.info(getHostID(SOSVfs_I_183.params("copy", source, target, getReplyString())));

            long size = size(target);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[copy][%s]size=%s", target, size));
            }
            return size;
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_185.params("copy", source, target), e);
        }
    }

}