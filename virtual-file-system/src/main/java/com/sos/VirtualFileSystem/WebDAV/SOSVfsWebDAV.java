package com.sos.VirtualFileSystem.WebDAV;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.contrib.ssl.StrictSSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.TrustMaterial;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;

import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @ressources webdavclient4j-core-0.92.jar
 * @author Robert Ehrlich */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsWebDAV extends SOSVfsTransferBaseClass {

    private static final Logger LOGGER = Logger.getLogger(SOSVfsWebDAV.class);
    private HttpURL rootUrl = null;
    private WebdavResource davClient = null;
    private String currentDirectory = "";
    private String password = null;
    private String proxyHost = null;
    private int proxyPort = 0;
    private String proxyUser = null;
    private String proxyPassword = null;
    private boolean simulateShell = false;

    public SOSVfsWebDAV() {
        super();
    }

    @Override
    public ISOSConnection Connect() throws Exception {
        this.Connect(this.connection2OptionsAlternate);
        return this;

    }

    @Override
    public ISOSConnection Connect(final SOSConnection2OptionsAlternate options) throws Exception {
        connection2OptionsAlternate = options;
        if (connection2OptionsAlternate == null) {
            RaiseException(SOSVfs_E_190.params("connection2OptionsAlternate"));
        }
        this.connect(connection2OptionsAlternate.host.Value(), connection2OptionsAlternate.port.value());
        return this;
    }

    @Override
    public ISOSConnection Authenticate(final ISOSAuthenticationOptions options) {
        authenticationOptions = options;
        try {
            proxyHost = connection2OptionsAlternate.proxy_host.Value();
            proxyPort = connection2OptionsAlternate.proxy_port.value();
            proxyUser = connection2OptionsAlternate.proxy_user.Value();
            proxyPassword = connection2OptionsAlternate.proxy_password.Value();
            this.doAuthenticate(authenticationOptions);
        } catch (JobSchedulerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
        }
        return this;
    }

    @Override
    public void login(final String pUserName, final String pPassword) {
        String method = "login";
        try {
            userName = pUserName;
            password = pPassword;
            LOGGER.debug(SOSVfs_D_132.params(userName));
            HttpURL httpUrl = this.setRootHttpURL(userName, pPassword, host, port);
            davClient = getWebdavResource(httpUrl);
            if (!davClient.exists()) {
                throw new Exception(String.format("%s: HTTP-DAV isn't enabled %s ", method, getStatusMessage(davClient)));
            }
            reply = "OK";
            LOGGER.info(SOSVfs_D_133.params(userName));
            this.LogReply();
        } catch (Exception e) {
            RaiseException(e, SOSVfs_E_134.params("authentication"));
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
        this.logINFO(reply);
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
            LOGGER.debug(HostID(SOSVfs_D_179.params("mkdir", path)));
            String[] subfolders = objF.getSubFolderArrayReverse();
            int idx = subfolders.length;
            for (String strSubFolder : objF.getSubFolderArrayReverse()) {
                res = this.getResource(strSubFolder);
                if (res.isCollection()) {
                    LOGGER.debug(SOSVfs_E_180.params(strSubFolder));
                    break;
                } else if (res.exists()) {
                    RaiseException(SOSVfs_E_277.params(strSubFolder));
                    break;
                }
                idx--;
            }
            subfolders = objF.getSubFolderArray();
            for (int i = idx; i < subfolders.length; i++) {
                subfolders[i] = getWebdavRessourcePath(subfolders[i]);
                if (davClient.mkcolMethod(subfolders[i])) {
                    reply = "mkdir OK";
                    LOGGER.debug(HostID(SOSVfs_E_0106.params("mkdir", subfolders[i], getReplyString())));
                } else {
                    throw new Exception(getStatusMessage(davClient));
                }
            }
        } catch (Exception e) {
            reply = e.toString();
            RaiseException(e, SOSVfs_E_134.params("[mkdir]"));
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
            path = this.normalizePath(path + "/");
            if (davClient.deleteMethod(path)) {
                reply = "rmdir OK";
                LOGGER.debug(HostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
            } else {
                throw new JobSchedulerException(getStatusMessage(davClient));
            }
            LOGGER.info(HostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
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
    public String[] listNames(String path) throws IOException {
        WebdavResource res = null;
        try {
            if (path.isEmpty()) {
                path = ".";
            }
            res = this.getResource(path);
            if (!res.exists()) {
                return null;
            }
            if (!res.isCollection()) {
                reply = "ls OK";
                return new String[] { path };
            }
            WebdavResource[] lsResult = res.listWebdavResources();
            String[] result = new String[lsResult.length];
            String curDir = getCurrentPath();
            for (int i = 0; i < lsResult.length; i++) {
                WebdavResource entry = lsResult[i];
                result[i] = entry.getPath();
                if (result[i].startsWith(curDir)) {
                    result[i] = result[i].substring(curDir.length());
                }
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
    public long getFile(String remoteFile, final String localFile, final boolean append) {
        String sourceLocation = this.resolvePathname(remoteFile);
        File transferFile = null;
        long remoteFileSize = -1;
        File file = null;
        WebdavResource res = null;
        try {
            remoteFile = this.normalizePath(remoteFile);
            file = new File(localFile);
            res = this.getResource(remoteFile);
            if (!res.exists()) {
                throw new Exception("remoteFile not found");
            }
            remoteFileSize = res.getGetContentLength();
            if (res.getMethod(sourceLocation, file)) {
                transferFile = new File(localFile);
                if (!append && remoteFileSize > 0 && remoteFileSize != transferFile.length()) {
                    throw new JobSchedulerException(SOSVfs_E_162.params(remoteFileSize, transferFile.length()));
                }
                remoteFileSize = transferFile.length();
                reply = "get OK";
                logINFO(HostID(SOSVfs_I_182.params("getFile", sourceLocation, localFile, getReplyString())));
            } else {
                throw new Exception(res.getStatusMessage());
            }
        } catch (Exception ex) {
            reply = ex.toString();
            RaiseException(ex, SOSVfs_E_184.params("getFile", sourceLocation, localFile));
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (Exception e) {
                //
            }
        }
        return remoteFileSize;
    }

    @Override
    public long putFile(final String localFile, String remoteFile) {
        long size = 0;
        try {
            remoteFile = getWebdavRessourcePath(remoteFile);
            remoteFile = this.normalizePath(remoteFile);
            if (davClient.putMethod(remoteFile, new File(localFile))) {
                reply = "put OK";
                logINFO(HostID(SOSVfs_I_183.params("putFile", localFile, remoteFile, getReplyString())));
                return this.size(remoteFile);
            } else {
                throw new Exception(getStatusMessage(davClient));
            }
        } catch (Exception e) {
            reply = e.toString();
            RaiseException(e, SOSVfs_E_185.params("putFile()", localFile, remoteFile));
        }
        return size;
    }

    @Override
    public void delete(String path) {
        try {
            path = getWebdavRessourcePath(path);
            path = this.normalizePath(path);
            if (this.isDirectory(path)) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }
            if (!davClient.deleteMethod(path)) {
                throw new Exception(getStatusMessage(davClient));
            }
        } catch (Exception ex) {
            reply = ex.toString();
            RaiseException(ex, SOSVfs_E_187.params("delete", path));
        }
        reply = "rm OK";
        logINFO(HostID(SOSVfs_D_181.params("delete", path, getReplyString())));
    }

    @Override
    public void rename(String from, String to) {
        from = this.resolvePathname(from);
        to = this.resolvePathname(to);
        try {
            from = getWebdavRessourcePath(from);
            from = this.normalizePath(from);
            to = getWebdavRessourcePath(to);
            to = this.normalizePath(to);
            if (!davClient.moveMethod(from, to)) {
                throw new Exception(getStatusMessage(davClient));
            }
        } catch (Exception e) {
            reply = e.toString();
            RaiseException(e, SOSVfs_E_188.params("rename", from, to));
        }
        reply = "mv OK";
        logINFO(HostID(SOSVfs_I_189.params(from, to, getReplyString())));
    }

    @Override
    public void ExecuteCommand(final String cmd) {
        LOGGER.debug("not implemented yet");
    }

    @Override
    public InputStream getInputStream(String path) {
        try {
            path = getWebdavRessourcePath(path);
            return davClient.getMethodData(path);
        } catch (Exception ex) {
            RaiseException(ex, SOSVfs_E_193.params("getInputStream()", path));
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream(final String path) {
        WebdavResource res = null;
        try {
            res = this.getResource(path, false);
            return new SOSVfsWebDAVOutputStream(res);
        } catch (Exception ex) {
            RaiseException(ex, SOSVfs_E_193.params("getOutputStream()", path));
            return null;
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
    public boolean changeWorkingDirectory(String path) {
        try {
            String origPath = davClient.getPath();
            path = this.normalizePath("/" + path + "/");
            davClient.setPath(path);
            if (davClient.exists()) {
                reply = "cwd OK";
                currentDirectory = path;
                LOGGER.debug(SOSVfs_D_194.params(path, getReplyString()));
            } else {
                davClient.setPath(origPath);
                reply = "cwd failed";
                LOGGER.debug(SOSVfs_D_194.params(path, getReplyString()));
                return false;
            }
        } catch (Exception ex) {
            RaiseException(ex, SOSVfs_E_193.params("cwd", path));
        }
        return true;
    }

    @Override
    public ISOSVirtualFile getFileHandle(String fileName) {
        fileName = adjustFileSeparator(fileName);
        ISOSVirtualFile file = new SOSVfsWebDAVFile(fileName);
        file.setHandler(this);
        return file;
    }

    @Override
    public String getModificationTime(final String path) {
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
                LOGGER.debug(HostID(SOSVfs_D_195.params(currentDirectory)));
                LogReply();
            }
        } catch (Exception e) {
            RaiseException(e, SOSVfs_E_134.params("getCurrentPath"));
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
        path = this.normalizePath(path);
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

    private String normalizePath(String path) {
        return path.replaceAll("//+", "/");
    }

    private HttpURL setRootHttpURL(final String puser, final String ppassword, final String phost, final int pport) throws Exception {
        rootUrl = null;
        HttpURL httpUrl = null;
        String path = "/";
        if (connection2OptionsAlternate.auth_method.isURL()) {
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
                psf.setCheckHostname(connection2OptionsAlternate.verify_certificate_hostname.value());
                if (!psf.getCheckHostname()) {
                    LOGGER.info("*********************** Security warning *********************************************************************");
                    LOGGER.info("Jade option \"verify_certificate_hostname\" is currently \"false\". ");
                    LOGGER.info("The certificate verification process will not verify the DNS name of the certificate presented by the server,");
                    LOGGER.info("with the hostname of the server in the URL used by the Jade client.");
                    LOGGER.info("**************************************************************************************************************");
                }
                if (connection2OptionsAlternate.accept_untrusted_certificate.value()) {
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

    private ISOSConnection doAuthenticate(final ISOSAuthenticationOptions options) throws Exception {
        authenticationOptions = options;
        userName = authenticationOptions.getUser().Value();
        password = authenticationOptions.getPassword().Value();
        LOGGER.debug(SOSVfs_D_132.params(userName));
        HttpURL httpUrl = this.setRootHttpURL(userName, password, host, port);
        try {
            if (!SOSString.isEmpty(proxyHost)) {
                LOGGER.info(String.format("using proxy: %s:%s, proxy user = %s", proxyHost, proxyPort, proxyUser));
            }
            davClient = getWebdavResource(getWebdavRessourceURL(httpUrl));
            if (!davClient.exists()) {
                throw new JobSchedulerException(getStatusMessage(davClient));
            }
        } catch (JobSchedulerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_167.params(authenticationOptions.getAuth_method().Value(),
                    authenticationOptions.getAuth_file().Value()), ex);
        }
        reply = "OK";
        LOGGER.info(SOSVfs_D_133.params(userName));
        this.LogReply();
        return this;
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
            if (uri.toLowerCase().startsWith("https://") && client.getStatusCode() == 0
                    && !connection2OptionsAlternate.accept_untrusted_certificate.value()) {
                msg += " maybe is this the problem by using of a self-signed certificate (option accept_untrusted_certificate = false)";
            }
        }
        String proxy = "";
        if (!SOSString.isEmpty(proxyHost)) {
            proxy = String.format("[proxy %s:%s, proxy user = %s]", proxyHost, proxyPort, proxyUser);
        }
        return String.format("[%s]%s %s", uri, proxy, msg);
    }

    private void connect(final String phost, final int pport) throws Exception {
        host = phost;
        port = pport;
        if (connection2OptionsAlternate.auth_method.isURL()) {
            URL url = new URL(phost);
            port = (url.getPort() == -1) ? url.getDefaultPort() : url.getPort();
        }
        LOGGER.info(SOSVfs_D_0101.params(host, port));
        if (!this.isConnected()) {
            this.LogReply();
        } else {
            logWARN(SOSVfs_D_0103.params(host, port));
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
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