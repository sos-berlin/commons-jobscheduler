package com.sos.vfs.ftp.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSEnv;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSFileEntry.EntryType;
import com.sos.vfs.common.SOSVFSMessageCodes;
import com.sos.vfs.common.interfaces.ISOSProvider;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.ftp.SOSFTPFile;

import sos.util.SOSDate;
import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFTPBaseClass extends SOSVFSMessageCodes implements ISOSProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFTPBaseClass.class);
    private static final String CLASS_NAME = SOSFTPBaseClass.class.getSimpleName();

    private SOSBaseOptions baseOptions = null;
    private SOSProviderOptions providerOptions = null;
    private SOSFTPServerReply ftpReply = null;
    private SOSOptionTransferMode transferMode = null;
    private SOSFTPClientLogger commandListener = null;
    private List<SOSFileEntry> directoryListing = null;
    private FTPClient ftpClient = null;
    private String host = EMPTY_STRING;
    private int port = 0;
    private String user = EMPTY_STRING;
    private String currentPath = EMPTY_STRING;
    private String reply = EMPTY_STRING;

    private SOSOptionProxyProtocol proxyProtocol = null;
    private String proxyHost = null;
    private int proxyPort = 0;
    private String proxyUser = null;
    private String proxyPassword = null;

    public SOSFTPBaseClass() {
        super("SOSVirtualFileSystem");
    }

    @Override
    public boolean isConnected() {
        boolean isConnected = false;
        if (getClient().isConnected()) {
            try {
                getClient().sendCommand("NOOP");
                isConnected = true;
            } catch (IOException e) {
                //
            }
        }
        return isConnected;
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        final String method = CLASS_NAME + "::Connect";
        providerOptions = options;
        try {
            host = providerOptions.host.getValue();
            port = providerOptions.port.value();
            proxyProtocol = providerOptions.proxyProtocol;
            proxyHost = providerOptions.proxyHost.getValue();
            proxyPort = providerOptions.proxyPort.value();
            proxyUser = providerOptions.proxyUser.getValue();
            proxyPassword = providerOptions.proxyPassword.getValue();
            doConnect();
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }

        login();
    }

    private void login() throws Exception {
        if (providerOptions == null) {
            throw new Exception("providerOptions is null");
        }
        user = providerOptions.user.getValue();
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(SOSVfs_D_132.params(user));
            }
            getClient().login(user, providerOptions.password.getValue());
            logReply("login");
            if (ftpReply.isSuccessCode()) {
                commandListener.setClientId(getHostID(""));
                LOGGER.info(getHostID(SOSVfs_D_133.params(user)));

                if (providerOptions.passiveMode.value()) {
                    passive();
                }
                transferMode(providerOptions.transferMode);

                try {
                    doPostLoginOperations();
                } catch (Exception e) {
                    //
                }
            } else {
                LOGGER.info(SOSVfs_D_132.params(user));
                throw new JobSchedulerException(SOSVfs_E_134.params("Login") + " code:" + ftpReply.getCode() + " Message: " + ftpReply
                        .getMessages()[0]);
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    @Override
    public void reconnect() {
        if (!isConnected()) {
            try {
                connect(providerOptions);
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Exception e) {
                throw new JobSchedulerException(e);
            }
        }
    }

    @Override
    public void disconnect() {
        try {
            if (getClient().isConnected()) {
                getClient().disconnect();
            }
        } catch (IOException e) {
            LOGGER.warn(SOSVfs_W_136.get() + e.toString(), e);
        }
    }

    protected void doConnect() {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(SOSVfs_D_0101.params(host, port));
            }

            setConnectTimeout();
            getClient().connect(host, port);
            logReply("connect");
            LOGGER.info(SOSVfs_D_0102.params(host, port));
            getClient().setControlKeepAliveTimeout(180);

        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(e.getClass().getName() + " - " + e.getMessage()), e);
        }
    }

    private String ms2string(int val) {
        if (val <= 0) {
            return String.valueOf(val).concat("ms");
        }
        try {
            return String.valueOf(Math.round(val / 1000)).concat("s");
        } catch (Throwable e) {
            return String.valueOf(val).concat("ms");
        }
    }

    private void setConnectTimeout() throws Exception {
        String ct = providerOptions.connect_timeout.getValue();
        int timeout = 0;
        if (!SOSString.isEmpty(ct)) {
            timeout = SOSDate.resolveAge("ms", ct).intValue();
        }
        if (timeout > 0) {
            try {
                getClient().setConnectTimeout(timeout);
                LOGGER.info("ConnectTimeout=" + ms2string(getClient().getConnectTimeout()));
            } catch (Exception ex) {
                LOGGER.warn(String.format("[setConnectTimeout]%s", ex.toString()), ex);
            }
        }
    }

    @Override
    public ISOSProviderFile getFile(final String filename) {
        ISOSProviderFile file = new SOSFTPFile(SOSCommonProvider.normalizePath(filename));
        file.setProvider(this);
        return file;
    }

    @Override
    public SOSFileEntry getFileEntry(final String pathname) throws Exception {
        FTPFile file = getFTPFile(pathname);
        if (file == null) {
            return null;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s]found", pathname));
        }
        Path parent = Paths.get(pathname).getParent();
        return getFileEntry(file, parent == null ? null : parent.toString());
    }

    public SOSFileEntry getFileEntry(FTPFile file, String parentPath) {
        if (file == null) {
            return null;
        }
        SOSFileEntry entry = new SOSFileEntry(EntryType.FILESYSTEM);
        entry.setDirectory(file.isDirectory());
        entry.setFilename(file.getName());
        entry.setFilesize(file.getSize());
        entry.setParentPath(parentPath);
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException {
        try {

            List<SOSFileEntry> result = new ArrayList<>();
            if (path.isEmpty()) {
                path = ".";
            }
            if (checkIfExists && !fileExists(path)) {
                return result;
            }
            if (checkIfIsDirectory && !isDirectory(path)) {
                reply = "ls OK";
                return result;
            }

            FTPFile[] list = null;
            try {
                list = getClient().listFiles(path);
            } catch (IOException e) {
                throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params("getFilenames")), e);
            }
            if (list == null || list.length <= 0) {
                if (isNegativeCommandCompletion()) {
                    throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params("getFilenames")) + ":" + getReplyString());
                }
                return result;
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s][ls] %s files or folders", path, list.length));
            }

            for (FTPFile file : list) {
                String name = file.getName();

                if (!name.trim().isEmpty() && isNotHiddenFile(name)) {
                    result.add(getFileEntry(file, path));
                }
            }
            reply = "ls OK";
            return result;
        } catch (Exception e) {
            reply = e.toString();
            return null;
        }
    }

    private List<SOSFileEntry> getFilenames(String path, final boolean recursive, int recLevel, boolean checkIfExists) throws Exception {
        if (recLevel == 0) {
            directoryListing = new ArrayList<SOSFileEntry>();
        }
        List<SOSFileEntry> entries = null;
        path = path.trim();
        if (path.isEmpty()) {
            path = ".";
        }
        try {
            entries = listNames(path, checkIfExists, checkIfExists);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (entries == null) {
            return directoryListing;
        }
        for (SOSFileEntry entry : entries) {
            if (!isNotHiddenFile(entry.getFilename())) {
                continue;
            }
            if (entry.isDirectory()) {
                if (recursive) {
                    recLevel++;
                    getFilenames(entry.getFullPath(), recursive, recLevel, checkIfExists);
                }
            } else {
                directoryListing.add(entry);
            }
        }
        return directoryListing;
    }

    @Override
    public List<SOSFileEntry> getFilelist(final String folder, final String regexp, final int flag, final boolean recursive, boolean checkIfExists,
            String integrityHashType) throws Exception {
        List<SOSFileEntry> result = getFilenames(folder, recursive, 0, checkIfExists);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s][total] %s files", folder, result.size()));
        }

        List<SOSFileEntry> list = new ArrayList<SOSFileEntry>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (SOSFileEntry entry : result) {
            if (entry.isDirectory()) {
                continue;
            }
            // file list should not contain the checksum files
            if (integrityHashType != null && entry.getFilename().endsWith(integrityHashType)) {
                continue;
            }
            Matcher matcher = pattern.matcher(entry.getFilename());
            if (matcher.find()) {
                list.add(entry);
            }
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s][filtered] %s files", folder, list.size()));
        }
        return list;
    }

    @Override
    public List<SOSFileEntry> getFolderlist(final String folder, final String regexp, final int flag, final boolean recursive) throws Exception {
        List<SOSFileEntry> result = getFilenames(folder, recursive, 0, false);

        List<SOSFileEntry> list = new ArrayList<SOSFileEntry>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (SOSFileEntry entry : result) {
            if (!entry.isDirectory()) {
                continue;
            }
            Matcher matcher = pattern.matcher(entry.getFilename());
            if (matcher.find()) {
                list.add(entry);
            }
        }
        return list;
    }

    private FTPFile getFTPFile(final String fileName) {
        final String method = CLASS_NAME + "::getFTPFile";
        FTPFile file = null;
        try {
            FTPFile[] list = getClient().listFiles(fileName);
            if (isNotNull(list) && list.length > 0) {
                file = list[0];
            }
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
        return file;
    }

    private int cd(final String directory) throws IOException {
        return getClient().cwd(directory);
    }

    private int doCD(final String folderName) {
        int x = 0;
        try {
            String path = SOSCommonProvider.normalizePath(folderName);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(SOSVfs_D_127.params(path));
            }
            x = cd(path);
            logReply("cd][" + path);
        } catch (SocketException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params("doCD")), e);
        } catch (IOException e) {
            LOGGER.debug(e.toString(), e);
        }
        return x;
    }

    public void completePendingCommand() {
        final String method = CLASS_NAME + "::CompletePendingCommand";
        LOGGER.trace("completePendingCommand");
        try {
            if (!getClient().completePendingCommand()) {
                disconnect();
                throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
        if (isNegativeCommandCompletion()) {
            throw new JobSchedulerException(SOSVfs_E_124.params(getReplyString()));
        }
    }

    private void doPostLoginOperations() {
        getClient().setControlKeepAliveTimeout(180);
        String msg;
        try {
            msg = getClient().getSystemType();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("System-Type = %1$s", msg));
            }
        } catch (IOException e) {
            LOGGER.info(e.toString(), e);
        }
        sendCommand("FEAT");
        if (ftpReply.getCode() == FTPReply.SYSTEM_STATUS) {
            String[] lines = ftpReply.getMessages();
            for (int i = 1; i < lines.length - 1; i++) {
                String feat = lines[i].trim().toUpperCase();
                if ("UTF8".equals(feat)) {
                    getClient().setControlEncoding("UTF-8");
                    break;
                }
            }
        } else {
            LOGGER.info("no valid response for FEAT command received: " + ftpReply.toString());
        }
        sendCommand("NOOP");
    }

    @Override
    public void executeCommand(final String cmd) throws Exception {
        executeCommand(cmd, null);
    }

    @Override
    public void executeCommand(final String cmd, SOSEnv env) throws Exception {
        String command = cmd.trim();
        try {
            getClient().sendCommand(command);
            String replyString = getClient().getReplyString().trim();
            int replyCode = getClient().getReplyCode();
            if (FTPReply.isNegativePermanent(replyCode) || FTPReply.isNegativeTransient(replyCode) || replyCode >= 10_000) {
                throw new JobSchedulerException(SOSVfs_E_164.params(replyString + "[" + command + "]"));
            }
            LOGGER.info(SOSVfs_D_151.params(command, replyString));
        } catch (JobSchedulerException ex) {
            if (providerOptions.raiseExceptionOnError.value()) {
                throw ex;
            }
            LOGGER.info(SOSVfs_D_151.params(command, ex.toString()), ex);
        } catch (Exception ex) {
            if (providerOptions.raiseExceptionOnError.value()) {
                throw new JobSchedulerException(SOSVfs_E_134.params("ExecuteCommand"), ex);
            }
            LOGGER.info(SOSVfs_D_151.params(command, ex.toString()), ex);
        }
    }

    private void sendCommand(final String command) {
        try {
            getClient().sendCommand(command);
        } catch (IOException e) {
            throw new JobSchedulerException("command failed: " + command, e);
        }
        logReply("sendCommand][" + command);
    }

    private final String getCurrentPath() {
        final String method = CLASS_NAME + "::getCurrentPath";
        try {
            getClient().pwd();
            String pwd = getReplyString();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(getHostID(SOSVfs_E_0106.params(method, "", pwd)));
            }
            return pwd.replaceFirst("^[^\"]*\"([^\"]*)\".*", "$1");
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    @Override
    public final boolean isDirectory(final String path) {
        boolean result = false;
        if (isNotHiddenFile(path)) {
            if (currentPath.isEmpty()) {
                currentPath = getCurrentPath();
            }
            if (currentPath.replaceFirst("/$", "").equals(path.replaceFirst("/$", ""))) {
                result = true;
            } else {
                doCD(path);
                if (isPositiveCommandCompletion()) {
                    doCD(currentPath);
                    result = true;
                }
            }
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s]%s", path, result));
        }
        return result;
    }

    @Override
    public boolean directoryExists(final String fileName) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]directoryExists", fileName));
        }
        return isDirectory(fileName);
    }

    @Override
    public boolean fileExists(String fileName) {
        boolean result = false;
        if (getFileSize(fileName) >= 0) {
            result = true;
        }
        LOGGER.debug(String.format("[%s]%s", fileName, result));
        return result;
    }

    @Override
    public long getFileSize(final String fileName) {
        final String method = CLASS_NAME + "::getFileSize";
        long size = -1;
        try {
            size = size(fileName);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_153.params(method, e));
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s]%s", fileName, size));
        }
        return size;
    }

    public long size(final String path) throws Exception {
        long size = -1L;
        if (transferMode.isAscii()) {
            this.binary();
        }
        getClient().sendCommand("SIZE " + path);
        logReply("size][" + path);
        if (getClient().getReplyCode() == FTPReply.FILE_STATUS) {
            size = Long.parseLong(trimResponseCode(getReplyString()));
        }
        if (transferMode.isAscii()) {
            ascii();
        }
        return size;
    }

    @Override
    public String getModificationDateTime(final String strFileName) {
        final String method = CLASS_NAME + "::getModificationTime";
        try {
            return getClient().getModificationTime(strFileName);
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    @Override
    public InputStream getInputStream(String fileName) {
        final String method = CLASS_NAME + "::getInputStream";
        InputStream is = null;
        try {
            fileName = fileName.replaceAll("\\\\", "/");
            is = getClient().retrieveFileStream(fileName);
            if (is == null) {
                throw new JobSchedulerException(String.format("unable to get inputstream for file '%1$s'", fileName));
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        } finally {
            logReply("getInputStream][" + fileName);
        }
        return is;
    }

    @Override
    public OutputStream getOutputStream(final String fileName, boolean append, boolean resume) {
        OutputStream os = null;
        try {
            os = getClient().storeFileStream(fileName);
            logReply("getOutputStream][" + fileName);
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params("getOutputStream", e.toString())), e);
        }
        return os;
    }

    public OutputStream getAppendFileStream(final String fileName) {
        final String method = CLASS_NAME + "::getAppendFileStream";
        OutputStream os = null;
        try {
            os = getClient().appendFileStream(fileName);
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
        return os;
    }

    @Override
    public void delete(final String path, boolean checkIsDirectory) throws IOException {
        try {
            if (checkIsDirectory && isDirectory(path)) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }

            getClient().deleteFile(path);
            if (isNegativeCommandCompletion()) {
                throw new JobSchedulerException(SOSVfs_E_144.params("delete()", path, getReplyString()));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("delete()"), e);
        }
    }

    @Override
    public void mkdir(final String path) {
        final String method = CLASS_NAME + "::mkdir";
        try {
            if (isDirectory(path)) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(SOSVfs_E_180.params(path));
                }

            } else {
                SOSOptionFolderName folderName = new SOSOptionFolderName(path);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(getHostID(SOSVfs_D_179.params("mkdir", path)));
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
                    getClient().makeDirectory(subfolders[i]);
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(getHostID(SOSVfs_E_0106.params(method, subfolders[i], getReplyString())));
                    }
                }
            }
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    @Override
    public void rename(final String from, final String to) {
        try {
            getClient().rename(from, to);
            if (isNegativeCommandCompletion()) {
                throw new JobSchedulerException(SOSVfs_E_144.params("rename()", from, getReplyString()));
            } else {
                LOGGER.info(String.format(SOSVfs_I_150.params(from, to)));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("rename()"), e);
        }
    }

    @Override
    public final void rmdir(final String path) throws IOException {
        final String method = CLASS_NAME + "::rmdir";
        try {
            SOSOptionFolderName folderName = new SOSOptionFolderName(path);
            for (String folder : folderName.getSubFolderArrayReverse()) {
                String folderPath = folder + "/";
                getClient().removeDirectory(folderPath);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(getHostID(SOSVfs_E_0106.params(method, folderPath, getReplyString())));
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    protected Proxy getSocksProxy() {
        if (!SOSString.isEmpty(getProxyUser())) {
            Authenticator.setDefault(new Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getProxyUser(), getProxyPassword().toCharArray());
                }
            });
        }
        return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(getProxyHost(), getProxyPort()));
    }

    protected Proxy getHTTPProxy() {
        if (!SOSString.isEmpty(getProxyUser())) {
            Authenticator.setDefault(new Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getProxyUser(), getProxyPassword().toCharArray());
                }
            });
        }
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getProxyHost(), getProxyPort()));
    }

    public KeyStore loadKeyStore(String storeType, File storePath, String storePass) throws KeyStoreException, IOException, GeneralSecurityException {
        KeyStore ks = KeyStore.getInstance(storeType);
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(storePath);
            ks.load(stream, storePass.toCharArray());
        } finally {
            Util.closeQuietly(stream);
        }
        return ks;
    }

    // @Override
    private final int passive() {
        final String method = CLASS_NAME + "::passive";

        try {
            int i = getClient().pasv();
            if (isPositiveCommandCompletion() == false) {
                throw new JobSchedulerException(getHostID(SOSVfs_E_0106.params("pasv", "", getReplyString())));
            } else {
                LOGGER.info(getHostID(SOSVfs_E_0106.params("pasv", "", getReplyString())));
                getClient().enterLocalPassiveMode();
            }
            return i;
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    private final ISOSProviderFile transferMode(final SOSOptionTransferMode mode) {
        transferMode = mode;
        if (transferMode.isAscii()) {
            ascii();
        } else {
            binary();
        }
        LOGGER.info(SOSVfs_D_123.params(transferMode.getDescription(), getReplyString()));
        return null;
    }

    private void ascii() {
        try {
            if (!getClient().setFileType(FTP.ASCII_FILE_TYPE)) {
                throw new JobSchedulerException(SOSVfs_E_149.params(getReplyString()));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("ascii", e));
        }
    }

    private void binary() {
        try {
            if (!getClient().setFileType(FTP.BINARY_FILE_TYPE)) {
                throw new JobSchedulerException(SOSVfs_E_149.params(getReplyString()));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("setFileType to binary"), e);
        }
    }

    protected void closeInput(InputStream is) {
        try {
            if (is != null) {
                is.close();
                is = null;
            }
        } catch (IOException e) {
            //
        }
    }

    protected void closeObject(OutputStream os) {
        try {
            if (os != null) {
                os.flush();
                os.close();
                os = null;
            }
        } catch (Exception e) {
            //
        }
    }

    @Override
    public final String getReplyString() {
        String msg = getClient().getReplyString();
        if (msg != null) {
            msg = msg.trim();
        }
        ftpReply = new SOSFTPServerReply(msg);
        return msg;
    }

    @Override
    public boolean isNegativeCommandCompletion() {
        int x = getClient().getReplyCode();
        return x > 300;
    }

    public boolean isNotHiddenFile(final String fileName) {
        if (fileName == null || ".".equals(fileName) || "..".equals(fileName) || fileName.endsWith("/..") || fileName.endsWith("/.")) {
            return false;
        }
        return true;
    }

    protected boolean isPositiveCommandCompletion() {
        int x = getClient().getReplyCode();
        return x <= 300;
    }

    protected boolean logReply(String caller) {
        reply = getReplyString();
        if (LOGGER.isTraceEnabled()) {
            if (providerOptions.protocolCommandListener.isFalse() && !SOSString.isEmpty(reply)) {
                LOGGER.trace(String.format("[%s]%s", caller, reply));
            }
        }
        return true;
    }

    public String getResponse() {
        return this.getReplyString();
    }

    protected final String getHostID(final String msg) {
        return "(" + user + "@" + host + ":" + port + ") " + msg;
    }

    protected boolean usingProxy() {
        return !SOSString.isEmpty(getProxyHost());
    }

    protected boolean usingHttpProxy() {
        return getProxyProtocol() != null && getProxyProtocol().isHttp();
    }

    private String trimResponseCode(final String response) throws Exception {
        if (response.length() < 5) {
            return response;
        }
        return response.substring(4).trim();
    }

    @Override
    public boolean isSFTP() {
        return false;
    }

    public SOSOptionProxyProtocol getProxyProtocol() {
        return proxyProtocol;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public SOSProviderOptions getProviderOptions() {
        return providerOptions;
    }

    public SOSFTPServerReply getFtpReply() {
        return ftpReply;
    }

    public void setCurrentPath(String val) {
        currentPath = val;
    }

    public SOSFTPClientLogger getCommandListener() {
        return commandListener;
    }

    public void setCommandListener(SOSFTPClientLogger val) {
        commandListener = val;
    }

    public FTPClient getClient() {
        return ftpClient;
    }

    public void setClient(FTPClient val) {
        ftpClient = val;
    }

    public SOSBaseOptions getBaseOptions() {
        return baseOptions;
    }

    public void setBaseOptions(SOSBaseOptions val) {
        baseOptions = val;
    }

}