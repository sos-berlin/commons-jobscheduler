package com.sos.VirtualFileSystem.WebDAV;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.contrib.ssl.StrictSSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.TrustMaterial;
import org.apache.webdav.lib.WebdavResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSDestinationOptions;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSFileEntry.EntryType;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsWebDAV extends SOSVfsTransferBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsWebDAV.class);
    private HttpURL rootUrl = null;
    private WebdavResource davClient = null;
    private String currentDirectory = "";
    private String password = null;
    private String proxyHost = null;
    private int proxyPort = 0;
    private String proxyUser = null;
    private String proxyPassword = null;

    public SOSVfsWebDAV() {
        super();
    }

    @Override
    public void connect(final SOSDestinationOptions options) throws Exception {
        destinationOptions = options;
        if (destinationOptions == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("connection2OptionsAlternate"));
        }
        doConnect(destinationOptions.host.getValue(), destinationOptions.port.value());
    }

    @Override
    public void login(final ISOSAuthenticationOptions options) {
        authenticationOptions = options;
        try {
            proxyHost = destinationOptions.proxyHost.getValue();
            proxyPort = destinationOptions.proxyPort.value();
            proxyUser = destinationOptions.proxyUser.getValue();
            proxyPassword = destinationOptions.proxyPassword.getValue();
            doAuthenticate(authenticationOptions);
        } catch (JobSchedulerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
        }
    }

    @Override
    public void disconnect() {
        reply = "disconnect OK";
        if (davClient != null) {
            try {
                davClient.close();
                davClient = null;
            } catch (Exception ex) {
                reply = "disconnect: " + ex;
            }
        }
        LOGGER.info(reply);
    }

    @Override
    public boolean isConnected() {
        return davClient != null;
    }

    @Override
    public void mkdir(final String path) {
        WebdavResource res = null;
        try {
            SOSOptionFolderName objF = new SOSOptionFolderName(path);
            reply = "";
            LOGGER.debug(getHostID(SOSVfs_D_179.params("mkdir", path)));
            String[] subfolders = objF.getSubFolderArrayReverse();
            int idx = subfolders.length;
            for (String strSubFolder : objF.getSubFolderArrayReverse()) {
                res = this.getResource(strSubFolder);
                if (res.isCollection()) {
                    LOGGER.debug(SOSVfs_E_180.params(strSubFolder));
                    break;
                } else if (res.exists()) {
                    throw new JobSchedulerException(SOSVfs_E_277.params(strSubFolder));
                }
                idx--;
            }
            subfolders = objF.getSubFolderArray();
            for (int i = idx; i < subfolders.length; i++) {
                subfolders[i] = getWebdavRessourcePath(subfolders[i]);
                if (davClient.mkcolMethod(subfolders[i])) {
                    reply = "mkdir OK";
                    LOGGER.debug(getHostID(SOSVfs_E_0106.params("mkdir", subfolders[i], getReplyString())));
                } else {
                    throw new Exception(getStatusMessage(davClient));
                }
            }
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_134.params("[mkdir]"), e);
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ex) {
                    //
                }
            }
        }
    }

    @Override
    public void rmdir(String path) {
        try {
            reply = "rmdir OK";
            path = getWebdavRessourcePath(path);
            path = normalizePath(path + "/");
            if (davClient.deleteMethod(path)) {
                reply = "rmdir OK";
                LOGGER.debug(getHostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
            } else {
                throw new JobSchedulerException(getStatusMessage(davClient));
            }
            LOGGER.info(getHostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_134.params("[rmdir]"), e);
        }
    }

    @Override
    public boolean isDirectory(final String path) {
        WebdavResource res = null;
        try {
            res = this.getResource(path);
            return res.isCollection();
        } catch (Exception e) {
            //
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (Exception ex) {
                //
            }
        }
        return false;
    }

    @Override
    public SOSFileEntry getFileEntry(String path) throws Exception {
        WebdavResource file = getResource(path);
        if (file != null && !file.isCollection()) {
            return getFileEntry(file, file.getPath());// TODO parentPath
        }
        return null;
    }

    private SOSFileEntry getFileEntry(WebdavResource file, String parentPath) {
        SOSFileEntry entry = new SOSFileEntry(EntryType.HTTP);
        entry.setDirectory(file.isCollection());
        entry.setFilename(file.getName());
        entry.setFilesize(file.getGetContentLength());
        entry.setLastModified(file.getGetLastModified());
        entry.setParentPath(parentPath);
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException {
        WebdavResource res = null;
        try {
            List<SOSFileEntry> result = new ArrayList<>();
            if (path.isEmpty()) {
                path = ".";
            }
            res = this.getResource(path);
            if (checkIfExists && !res.exists()) {
                return result;
            }
            if (checkIfIsDirectory && !res.isCollection()) {
                reply = "ls OK";
                return result;
            }
            WebdavResource[] list = res.listWebdavResources();
            for (int i = 0; i < list.length; i++) {
                result.add(getFileEntry(list[i], path));
            }
            reply = "ls OK";
            return result;
        } catch (Exception e) {
            reply = e.toString();
            return null;
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ex) {
                    //
                }
            }
        }
    }

    @Override
    public long size(final String path) throws Exception {
        long size = -1;
        WebdavResource res = null;
        try {
            res = this.getResource(path);
            if (res.exists()) {
                size = res.getGetContentLength();
            }
        } catch (Exception e) {
            LOGGER.trace(e.getMessage());
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ex) {
                    //
                }
            }
        }
        return size;
    }

    @Override
    public void delete(String path, boolean checkIsDirectory) {
        try {
            path = getWebdavRessourcePath(path);
            path = normalizePath(path);
            if (checkIsDirectory && this.isDirectory(path)) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }
            if (!davClient.deleteMethod(path)) {
                throw new Exception(getStatusMessage(davClient));
            }
        } catch (Exception ex) {
            reply = ex.toString();
            throw new JobSchedulerException(SOSVfs_E_187.params("delete", path), ex);
        }
        reply = "rm OK";
        LOGGER.info(getHostID(SOSVfs_D_181.params("delete", path, getReplyString())));
    }

    @Override
    public void rename(String from, String to) {
        from = normalizePath(from);
        to = normalizePath(to);
        try {
            from = getWebdavRessourcePath(from);
            from = normalizeWebDavPath(from);
            to = getWebdavRessourcePath(to);
            to = normalizeWebDavPath(to);
            if (!davClient.moveMethod(from, to)) {
                throw new Exception(getStatusMessage(davClient));
            }
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_188.params("rename", from, to), e);
        }
        reply = "mv OK";
        LOGGER.info(getHostID(SOSVfs_I_189.params(from, to, getReplyString())));
    }

    @Override
    public void executeCommand(final String cmd) {
        LOGGER.debug("not implemented yet");
    }

    @Override
    public InputStream getInputStream(String path) {
        try {
            path = getWebdavRessourcePath(path);
            return davClient.getMethodData(path);
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getInputStream()", path), ex);
        }
    }

    @Override
    public OutputStream getOutputStream(final String path, boolean append, boolean resume) {
        WebdavResource res = null;
        try {
            res = this.getResource(path, false);
            return new SOSVfsWebDAVOutputStream(res);
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getOutputStream()", path), ex);
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (Exception e) {
                //
            }
        }
    }

    @Override
    public ISOSVirtualFile getFileHandle(String fileName) {
        fileName = adjustFileSeparator(fileName);
        ISOSVirtualFile file = new SOSVfsWebDAVFile(fileName);
        file.setHandler(this);
        return file;
    }

    @Override
    public String getModificationDateTime(final String path) {
        WebdavResource res = null;
        String dateTime = null;
        try {
            res = this.getResource(path);
            if (res.exists()) {
                long lm = res.getGetLastModified();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateTime = df.format(new Date(lm));
            }
        } catch (Exception ex) {
            //
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    //
                }
            }
        }
        return dateTime;
    }

    public long getModificationTimeStamp(final String path) throws Exception {
        WebdavResource res = null;
        try {
            res = this.getResource(path);
            if (res.exists()) {
                return res.getGetLastModified();
            }
            return -1L;
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    protected boolean fileExists(final String filename) {
        WebdavResource res = null;
        try {
            res = this.getResource(filename);
            return res.exists();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (Exception e) {
                //
            }
        }
    }

    @Override
    protected String getCurrentPath() {
        try {
            if (currentDirectory == null || currentDirectory.isEmpty()) {
                currentDirectory = "/" + davClient.getPath() + "/";
                currentDirectory = normalizePath(currentDirectory);
                LOGGER.debug(getHostID(SOSVfs_D_195.params(currentDirectory)));
                logReply();
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("getCurrentPath"), e);
        }

        return currentDirectory;
    }

    private WebdavResource getResource(final String path) throws Exception {
        return getResource(path, true);
    }

    private WebdavResource getWebdavResource(final HttpURL url) throws Exception {
        WebdavResource res = null;
        if (SOSString.isEmpty(proxyHost)) {
            res = new WebdavResource(url);
        } else {
            res = new WebdavResource(url, proxyHost, proxyPort, new UsernamePasswordCredentials(proxyUser, proxyPassword));
        }
        return res;
    }

    private WebdavResource getResource(String path, final boolean flgTryWithTrailingSlash) throws Exception {
        path = normalizePath(path);
        WebdavResource res = getWebdavResource(getWebdavRessourceURL(path));
        if (flgTryWithTrailingSlash && !path.endsWith("/") && !res.exists()) {
            WebdavResource res2 = null;
            try {
                res2 = getWebdavResource(getWebdavRessourceURL(path + "/"));
                if (res2.exists()) {
                    res = res2;
                }
            } catch (Exception e) {
                //
            } finally {
                try {
                    if (res2 != null) {
                        res2.close();
                    }
                } catch (Exception e) {
                    //
                }
            }
        }
        return res;
    }

    private String normalizeWebDavPath(String path) {
        return path.replaceAll("//+", "/");
    }

    private HttpURL setRootHttpURL(final String puser, final String ppassword, final String phost, final int pport) throws Exception {
        rootUrl = null;
        HttpURL httpUrl = null;
        String path = "/";
        if (destinationOptions.authMethod.isURL()) {
            URL url = new URL(phost);
            String phostRootUrl = url.getProtocol() + "://" + url.getAuthority();
            if (url.getPort() == -1) {
                phostRootUrl += ":" + url.getDefaultPort();
            }
            String normalizedHost = phostRootUrl + url.getPath();
            phostRootUrl += "/";
            if (!url.getPath().endsWith("/")) {
                normalizedHost += "/";
            }
            if ("https".equalsIgnoreCase(url.getProtocol())) {
                httpUrl = new HttpsURL(normalizedHost);
            } else {
                httpUrl = new HttpURL(normalizedHost);
            }
            if ("https".equalsIgnoreCase(httpUrl.getScheme())) {
                rootUrl = new HttpsURL(phostRootUrl);
                StrictSSLProtocolSocketFactory psf = new StrictSSLProtocolSocketFactory();
                psf.setCheckHostname(destinationOptions.verifyCertificateHostname.value());
                if (!psf.getCheckHostname()) {
                    LOGGER.info("*********************** Security warning *********************************************************************");
                    LOGGER.info("Jade option \"verify_certificate_hostname\" is currently \"false\". ");
                    LOGGER.info("The certificate verification process will not verify the DNS name of the certificate presented by the server,");
                    LOGGER.info("with the hostname of the server in the URL used by the Jade client.");
                    LOGGER.info("**************************************************************************************************************");
                }
                if (destinationOptions.acceptUntrustedCertificate.value()) {
                    psf.useDefaultJavaCiphers();
                    psf.addTrustMaterial(TrustMaterial.TRUST_ALL);
                }
                Protocol p = new Protocol("https", (ProtocolSocketFactory) psf, pport);
                Protocol.registerProtocol("https", p);
            } else {
                rootUrl = new HttpURL(phostRootUrl);
            }
        } else {
            httpUrl = new HttpURL(phost, pport, path);
            rootUrl = new HttpURL(phost, pport, path);
        }
        if (!SOSString.isEmpty(puser)) {
            httpUrl.setUserinfo(puser, ppassword);
        }
        return httpUrl;
    }

    private HttpURL getWebdavRessourceURL(String path) throws Exception {
        HttpURL url = null;
        if (rootUrl != null) {
            path = getWebdavRessourcePath(path);
            path = path.startsWith("/") ? path.substring(1) : path;
            if ("https".equalsIgnoreCase(rootUrl.getScheme())) {
                url = new HttpsURL(rootUrl.getEscapedURI() + path);
            } else {
                url = new HttpURL(rootUrl.getEscapedURI() + path);
            }
        }
        url.setUserinfo(userName, password);
        return url;
    }

    private String getWebdavRessourcePath(String path) {
        if (!path.startsWith("/")) {
            String curDir = getCurrentPath();
            path = curDir + path;
        }
        return path;
    }

    private HttpURL getWebdavRessourceURL(HttpURL url) throws Exception {
        url.setUserinfo(userName, password);
        return url;
    }

    private void doAuthenticate(final ISOSAuthenticationOptions options) throws Exception {
        authenticationOptions = options;
        userName = authenticationOptions.getUser().getValue();
        password = authenticationOptions.getPassword().getValue();
        LOGGER.debug(SOSVfs_D_132.params(userName));
        HttpURL httpUrl = this.setRootHttpURL(userName, password, host, port);
        try {
            if (!SOSString.isEmpty(proxyHost)) {
                LOGGER.info(String.format("using proxy: %s:%s, proxy user = %s", proxyHost, proxyPort, proxyUser));
            }
            davClient = getWebdavResource(getWebdavRessourceURL(httpUrl));
            if (!davClient.exists() && SOSString.isEmpty(davClient.getStatusMessage())) {
                davClient.setProperties(0);
            }
            if (!davClient.exists()) {
                throw new JobSchedulerException(getStatusMessage(davClient));
            }
        } catch (JobSchedulerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_167.params(authenticationOptions.getAuthMethod().getValue(), authenticationOptions.getAuthFile()
                    .getValue()), ex);
        }
        reply = "OK";
        LOGGER.info(SOSVfs_D_133.params(userName));
        this.logReply();
    }

    private String getStatusMessage(WebdavResource client) {
        String msg = client.getStatusMessage();
        String uri = "";
        try {
            uri = client.getHttpURL().getURI();
        } catch (Exception ex) {
            uri = String.format("unknown uri = %s", ex.toString());
        }
        if (SOSString.isEmpty(msg)) {
            msg = "no details provided.";
            if (uri.toLowerCase().startsWith("https://") && client.getStatusCode() == 0 && !destinationOptions.acceptUntrustedCertificate.value()) {
                msg += " maybe is this the problem by using of a self-signed certificate (option accept_untrusted_certificate = false)";
            }
        }
        String proxy = "";
        if (!SOSString.isEmpty(proxyHost)) {
            proxy = String.format("[proxy %s:%s, proxy user = %s]", proxyHost, proxyPort, proxyUser);
        }
        return String.format("[%s]%s %s", uri, proxy, msg);
    }

    private void doConnect(final String phost, final int pport) throws Exception {
        host = phost;
        port = pport;
        if (destinationOptions.authMethod.isURL()) {
            URL url = new URL(phost);
            port = (url.getPort() == -1) ? url.getDefaultPort() : url.getPort();
        }
        LOGGER.info(SOSVfs_D_0101.params(host, port));
        if (!this.isConnected()) {
            this.logReply();
        } else {
            LOGGER.warn(SOSVfs_D_0103.params(host, port));
        }
    }

}