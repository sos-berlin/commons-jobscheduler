package com.sos.VirtualFileSystem.FTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSSession;
import com.sos.VirtualFileSystem.Interfaces.ISOSShellOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFileSystem;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFolder;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSCommandResult;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSFileEntry.EntryType;
import com.sos.VirtualFileSystem.common.SOSVfsBaseClass;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtpBaseClass extends SOSVfsBaseClass implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsFtpBaseClass.class);
    private static final String CLASS_NAME = SOSVfsFtpBaseClass.class.getSimpleName();

    private ISOSConnectionOptions connectionOptions = null;
    private SOSConnection2OptionsAlternate connectionOptionsAlternate = null;
    private SOSFtpServerReply ftpReply = null;
    private SOSOptionTransferMode transferMode = null;
    private SOSFtpClientLogger commandListener = null;
    private List<SOSFileEntry> directoryListing = null;
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
    private boolean simulateShell = false;

    public SOSVfsFtpBaseClass() {
        super();
    }

    @Override
    public long appendFile(final String localFile, final String remoteFile) {
        long size;
        try {
            size = putFile(localFile, Client().appendFileStream(remoteFile));
            LOGGER.info(SOSVfs_I_155.params(size));
        } catch (IOException e) {
            throw new RuntimeException(SOSVfs_E_130.params("appendFileStream", e));
        }
        return size;
    }

    @Override
    public void ascii() {
        try {
            if (!Client().setFileType(FTP.ASCII_FILE_TYPE)) {
                throw new JobSchedulerException(SOSVfs_E_149.params(getReplyString()));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("ascii", e));
        }
    }

    @Override
    public ISOSConnection authenticate(final ISOSAuthenticationOptions options) throws Exception {
        login(options.getUser().getValue(), options.getPassword().getValue());
        return this;
    }

    @Override
    public void binary() {
        try {
            if (!Client().setFileType(FTP.BINARY_FILE_TYPE)) {
                throw new JobSchedulerException(SOSVfs_E_149.params(getReplyString()));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("setFileType to binary"), e);
        }
    }

    public int cd(final String directory) throws IOException {
        return Client().cwd(directory);
    }

    @Override
    public boolean changeWorkingDirectory(final String pathname) {
        try {
            Client().cwd(pathname);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(SOSVfs_D_135.params(pathname, getReplyString(), "[directory exists]"));
            }
        } catch (IOException e) {
            throw new RuntimeException(SOSVfs_E_130.params("cwd"), e);
        }
        return true;
    }

    protected FTPClient Client() {
        return null;
    }

    @Override
    public void close() {
        //
    }

    @Override
    public void closeConnection() throws Exception {
        if (Client().isConnected()) {
            Client().disconnect();
            if (connectionOptionsAlternate != null) {
                LOGGER.debug(SOSVfs_D_125.params(connectionOptionsAlternate.getHost().getValue()));
            }
            logReply();
        }
    }

    @Override
    public void closeInput() {
        //
    }

    private void closeInput(InputStream is) {
        try {
            if (is != null) {
                is.close();
                is = null;
            }
        } catch (IOException e) {
            //
        }
    }

    private void closeObject(OutputStream os) {
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
    public void closeOutput() {
        final String method = CLASS_NAME + "::closeOutput";
        try {
            completePendingCommand();
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    @Override
    public void closeSession() throws Exception {
        logout();
    }

    @Override
    public void completePendingCommand() {
        final String method = CLASS_NAME + "::CompletePendingCommand";
        LOGGER.trace("completePendingCommand");
        try {
            if (!Client().completePendingCommand()) {
                logout();
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

    public void doConnect(final String ftpHost, final int ftpPort) {
        try {
            host = ftpHost;
            port = ftpPort;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(SOSVfs_D_0101.params(host, port));
            }

            if (!isConnected()) {
                Client().connect(host, port);
                LOGGER.info(SOSVfs_D_0102.params(host, port));
                logReply();
                Client().setControlKeepAliveTimeout(180);
            } else {
                LOGGER.warn(SOSVfs_D_0103.params(host, port));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(e.getClass().getName() + " - " + e.getMessage()), e);
        }
    }

    @Override
    public ISOSConnection connect(final String host, final int port) throws Exception {
        doConnect(host, port);
        if (connectionOptions != null) {
            connectionOptions.getHost().setValue(host);
            connectionOptions.getPort().value(port);
        }
        return this;
    }

    @Override
    public ISOSConnection connect() {
        final String method = CLASS_NAME + "::Connect";

        host = connectionOptions.getHost().getValue();
        port = connectionOptions.getPort().value();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SOSVfs_D_0101.params(host, port));
        }
        try {
            if (connectionOptionsAlternate != null) {
                proxyProtocol = connectionOptionsAlternate.proxyProtocol;
                proxyHost = connectionOptionsAlternate.proxyHost.getValue();
                proxyPort = connectionOptionsAlternate.proxyPort.value();
                proxyUser = connectionOptionsAlternate.proxyUser.getValue();
                proxyPassword = connectionOptionsAlternate.proxyPassword.getValue();
            }
            doConnect(host, port);
            LOGGER.info(SOSVfs_D_0102.params(host, port));
            Client().setControlKeepAliveTimeout(180);
        } catch (RuntimeException e) {
            LOGGER.info(SOSVfs_E_0107.params(host, port, e.getMessage()));
            if (connectionOptions.getAlternativeHost().isDirty()) {
                String altHost = host = connectionOptions.getAlternativeHost().getValue();
                int altPort = port = connectionOptions.getAlternativePort().value();
                if (isNotEmpty(altHost) && altPort > 0) {
                    try {
                        JobSchedulerException.gflgStackTracePrinted = false;
                        connect(altHost, altPort);
                    } catch (Exception e1) {
                        LOGGER.info(SOSVfs_E_0107.params(host, port, e1.getMessage()));
                    }
                    LOGGER.info(SOSVfs_D_0102.params(altHost, altPort));
                } else {
                    LOGGER.info(SOSVfs_E_0107.params(host, port, e.getMessage()));
                    throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
                }
            } else {
                LOGGER.info(SOSVfs_E_0107.params(host, port, e.toString()), e);
                throw e;
            }
        }
        return this;
    }

    @Deprecated
    @Override
    public ISOSConnection connect(final ISOSConnectionOptions options) throws Exception {
        try {
            connectionOptions = options;
            this.connect();
        } catch (Exception e) {
            throw e;
        }
        return this;
    }

    @Override
    public ISOSConnection connect(final SOSConnection2OptionsAlternate options) {
        final String method = CLASS_NAME + "::Connect";
        connectionOptionsAlternate = options;
        try {
            host = connectionOptionsAlternate.host.getValue();
            port = connectionOptionsAlternate.port.value();
            proxyProtocol = connectionOptionsAlternate.proxyProtocol;
            proxyHost = connectionOptionsAlternate.proxyHost.getValue();
            proxyPort = connectionOptionsAlternate.proxyPort.value();
            proxyUser = connectionOptionsAlternate.proxyUser.getValue();
            proxyPassword = connectionOptionsAlternate.proxyPassword.getValue();
            connect(host, port);
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
        return this;
    }

    @Override
    public ISOSConnection connect(final ISOSDataProviderOptions options) throws Exception {
        return null;
    }

    @Override
    public String createScriptFile(final String content) throws Exception {
        notImplemented();
        return null;
    }

    @Override
    public void delete(final String path, boolean checkIsDirectory) throws IOException {
        try {
            if (checkIsDirectory && isDirectory(path)) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }

            Client().deleteFile(path);
            if (isNegativeCommandCompletion()) {
                throw new JobSchedulerException(SOSVfs_E_144.params("delete()", path, getReplyString()));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("delete()"), e);
        }
    }

    public List<SOSFileEntry> dir() {
        try {
            return dir(".");
        } catch (Exception e) {
            throw new RuntimeException(SOSVfs_E_130.params("dir"), e);
        }
    }

    @Override
    public List<SOSFileEntry> dir(final SOSFolderName folderName) {
        return dir(folderName.getValue());
    }

    public List<SOSFileEntry> dir(final String pathname) {
        return getFilenames(pathname);
    }

    @Override
    public SOSFileEntry getFileEntry(final String pathname) throws Exception {
        FTPFile file = getFTPFile(pathname);
        if (file == null) {
            return null;
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
    public List<SOSFileEntry> dir(final String pathname, final int flag) {
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        FTPFile[] list;
        try {
            list = Client().listFiles(pathname);
        } catch (IOException e) {
            throw new RuntimeException(SOSVfs_E_128.params("listfiles", "dir"), e);
        }
        for (FTPFile file : list) {
            SOSFileEntry entry = getFileEntry(file, pathname);

            if (flag > 0 && file.isDirectory()) {
                result.addAll(dir(pathname + "/" + file.getName(), flag >= 1024 ? flag : flag + 1024));
            } else {
                result.add(entry);
            }
        }
        return result;
    }

    @Override
    public void disconnect() {
        try {
            if (Client().isConnected()) {
                Client().disconnect();
            }
        } catch (IOException e) {
            LOGGER.warn(SOSVfs_W_136.get() + e.getMessage());
        }
    }

    private int doCD(final String folderName) {
        int x = 0;
        try {
            String path = folderName.replaceAll("\\\\", "/");
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(SOSVfs_D_127.params(path));
            }
            x = cd(path);
            logReply();
        } catch (SocketException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params("doCD")), e);
        } catch (IOException e) {
            LOGGER.debug(e.toString(), e);
        }
        return x;
    }

    protected int doCDUP() {
        final String method = CLASS_NAME + "::DoCDUP";
        try {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(SOSVfs_D_141.params("cdup"));
            }
            Client().cdup();
            logReply();
            doPWD();
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
        return 0;
    }

    @Override
    public void doPostLoginOperations() {
        Client().setControlKeepAliveTimeout(180);
        String msg;
        try {
            msg = Client().getSystemType();
            LOGGER.trace(String.format("System-Type = %1$s", msg));
        } catch (IOException e) {
            LOGGER.info(e.toString(), e);
        }
        sendCommand("FEAT");
        if (ftpReply.getCode() == FTPReply.SYSTEM_STATUS) {
            String[] lines = ftpReply.getMessages();
            for (int i = 1; i < lines.length - 1; i++) {
                String feat = lines[i].trim().toUpperCase();
                if ("UTF8".equals(feat)) {
                    Client().setControlEncoding("UTF-8");
                    break;
                }
            }
        } else {
            LOGGER.info("no valid response for FEAT command received: " + ftpReply.toString());
        }
        sendCommand("NOOP");
    }

    @Override
    public final String doPWD() {
        final String method = CLASS_NAME + "::DoPWD";
        try {
            LOGGER.trace(SOSVfs_D_141.params("pwd"));
            return getCurrentPath();
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    @Override
    public void executeCommand(final String cmd) throws Exception {
        executeCommand(cmd, null);
    }

    @Override
    public void executeCommand(final String cmd, SOSVfsEnv env) throws Exception {
        String command = cmd.endsWith("\n") ? cmd : cmd + "\n";
        sendCommand(command);
        LOGGER.info(SOSVfs_D_151.params(cmd, getReplyString()));
        Client().completePendingCommand();
    }

    @Override
    public void flush() {
        //
    }

    public void get(final String remoteFile, final String localFile) {
        FileOutputStream os = null;
        boolean rc = false;
        try {
            os = new FileOutputStream(localFile);
            rc = Client().retrieveFile(remoteFile, os);
            if (!rc) {
                throw new JobSchedulerException(SOSVfs_E_142.get());
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("get"), e);
        } finally {
            closeObject(os);
        }
    }

    @Override
    public OutputStream getAppendFileStream(final String fileName) {
        final String method = CLASS_NAME + "::getAppendFileStream";
        OutputStream os = null;
        try {
            os = Client().appendFileStream(fileName);
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
        return os;
    }

    @Override
    public ISOSConnection getConnection() {
        return this;
    }

    protected final String getCurrentPath() {
        final String method = CLASS_NAME + "::getCurrentPath";
        try {
            Client().pwd();
            String pwd = getReplyString();
            LOGGER.trace(getHostID(SOSVfs_E_0106.params(method, "", pwd)));
            return pwd.replaceFirst("^[^\"]*\"([^\"]*)\".*", "$1");
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    @Override
    public Integer getExitCode() {
        notImplemented();
        return null;
    }

    @Override
    public String getExitSignal() {
        notImplemented();
        return null;
    }

    @Override
    public long getFile(final String remoteFile, final String localFile) {
        return getFile(remoteFile, localFile, false);
    }

    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) {
        InputStream in = null;
        OutputStream out = null;
        long totalBytes = 0;
        try {
            in = Client().retrieveFileStream(remoteFile);
            if (in == null) {
                throw new JobSchedulerException(SOSVfs_E_143.params(remoteFile, getReplyString()));
            }
            if (!isPositiveCommandCompletion()) {
                throw new JobSchedulerException(SOSVfs_E_144.params("getFile()", remoteFile, getReplyString()));
            }
            byte[] buffer = new byte[4096];
            out = new FileOutputStream(new File(localFile), append);
            int bytes_read = 0;
            synchronized (this) {
                while ((bytes_read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes_read);
                    out.flush();
                    totalBytes += bytes_read;
                }
            }
            closeInput(in);
            closeObject(out);
            if (!Client().completePendingCommand()) {
                logout();
                disconnect();
                throw new JobSchedulerException(SOSVfs_E_134.params("File transfer"));
            }
            if (isNegativeCommandCompletion()) {
                throw new JobSchedulerException(SOSVfs_E_144.params("getFile()", remoteFile, getReplyString()));
            }
            if (totalBytes > 0) {
                return totalBytes;
            } else {
                return -1L;
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("getFile"), e);
        } finally {
            closeInput(in);
            closeObject(out);
        }
    }

    @Override
    public ISOSVirtualFile getFileHandle(final String filename) {
        ISOSVirtualFile file = new SOSVfsFtpFile(filename);
        file.setHandler(this);
        return file;
    }

    @Override
    public List<SOSFileEntry> getFilelist(final String folder, final String regexp, final int flag, final boolean recursive, boolean checkIfExists,
            String integrityHashType) {
        directoryListing = nList(folder, recursive, checkIfExists);

        List<SOSFileEntry> entries = new ArrayList<SOSFileEntry>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (SOSFileEntry entry : directoryListing) {
            // file list should not contain the checksum files
            if (integrityHashType != null && entry.getFilename().endsWith(integrityHashType)) {
                continue;
            }
            Matcher matcher = pattern.matcher(entry.getFilename());
            if (matcher.find()) {
                entries.add(entry);
            }
        }
        return entries;
    }

    private List<SOSFileEntry> getFilenames(final String pathname) {
        return getFilenames(pathname, false);
    }

    private List<SOSFileEntry> getFilenames(final String path, final boolean recursive) {
        return getFilenames(path, recursive, true);
    }

    private List<SOSFileEntry> getFilenames(final String path, final boolean recursive, final boolean checkReplyCode) {
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        FTPFile[] list = null;
        String pathName = path.trim();
        if (pathName == null) {
            pathName = "";
        }
        if (pathName.isEmpty()) {
            pathName = ".";
        }
        try {
            list = Client().listFiles(pathName);
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params("getFilenames")), e);
        }
        if (list == null || list.length <= 0) {
            if (isNegativeCommandCompletion()) {
                String message = getHostID(SOSVfs_E_0105.params("getFilenames")) + ":" + getReplyString();
                if (checkReplyCode) {
                    throw new JobSchedulerException(message);
                } else {
                    LOGGER.warn(message);
                }
            }
            return result;
        }
        for (FTPFile file : list) {
            String name = file.getName();
            if (isNotHiddenFile(name)) {
                if (name.indexOf("/") == -1) {
                    name = pathName + "/" + name;
                    name = name.replaceAll("//+", "/");
                }

                if (file.isFile()) {
                    result.add(getFileEntry(file, path));
                } else if (file.isDirectory() && recursive) {
                    List<SOSFileEntry> filelist = getFilenames(name + "/", recursive);
                    if (filelist != null && !filelist.isEmpty()) {
                        result.addAll(filelist);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public OutputStream getFileOutputStream() {
        return null;
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles() {
        return null;
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles(final String string) {
        return null;
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
        return size;
    }

    @Override
    public List<SOSFileEntry> getFolderlist(final String folder, final String regexp, final int flag, final boolean recursive) {
        directoryListing = null;
        if (directoryListing == null) {
            directoryListing = nList(folder, recursive, true);
        }
        List<SOSFileEntry> entries = new ArrayList<SOSFileEntry>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (SOSFileEntry entry : directoryListing) {
            Matcher matcher = pattern.matcher(entry.getFilename());
            if (matcher.find()) {
                entries.add(entry);
            }
        }
        return entries;
    }

    protected FTPFile getFTPFile(final String fileName) {
        final String method = CLASS_NAME + "::getFTPFile";
        FTPFile file = null;
        try {
            FTPFile[] list = Client().listFiles(fileName);
            if (isNotNull(list) && list.length > 0) {
                file = list[0];
            }
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
        return file;
    }

    @Override
    public ISOSVFSHandler getHandler() {
        return this;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public InputStream getInputStream(String fileName) {
        final String method = CLASS_NAME + "::getInputStream";
        InputStream is = null;
        try {
            fileName = fileName.replaceAll("\\\\", "/");
            is = Client().retrieveFileStream(fileName);
            if (is == null) {
                throw new JobSchedulerException(String.format("unable to get inputstream for file '%1$s'", fileName));
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        } finally {
            logReply();
        }
        return is;
    }

    @Override
    public String getModificationTime(final String strFileName) {
        final String method = CLASS_NAME + "::getModificationTime";
        try {
            return Client().getModificationTime(strFileName);
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    public SOSFileListEntry getNewVirtualFile(final String fileName) {
        return new SOSFileListEntry(fileName);
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public OutputStream getOutputStream(final String fileName) {
        OutputStream os = null;
        try {
            os = Client().storeFileStream(fileName);
            logReply();
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params("getOutputStream", e.toString())), e);
        }
        return os;
    }

    @Override
    public final String getReplyString() {
        String msg = Client().getReplyString();
        if (msg != null) {
            msg = msg.trim();
        }
        ftpReply = new SOSFtpServerReply(msg);
        return msg;
    }

    public String getResponse() {
        return this.getReplyString();
    }

    @Override
    public ISOSSession getSession() {
        return null;
    }

    @Override
    public StringBuffer getStdErr() throws Exception {
        return null;
    }

    @Override
    public StringBuffer getStdOut() throws Exception {
        return null;
    }

    protected final String getHostID(final String msg) {
        return "(" + user + "@" + host + ":" + port + ") " + msg;
    }

    @Override
    public boolean isConnected() {
        boolean isConnected = false;
        if (Client().isConnected()) {
            try {
                Client().sendCommand("NOOP");
                isConnected = true;
            } catch (IOException e) {
                //
            }
        }
        return isConnected;
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
        return result;
    }

    @Override
    public boolean isLoggedin() {
        return super.isLoggedin();
    }

    @Override
    public boolean isNegativeCommandCompletion() {
        int x = Client().getReplyCode();
        return x > 300;
    }

    public boolean isNotHiddenFile(final String fileName) {
        if (fileName == null || ".".equals(fileName) || "..".equals(fileName) || fileName.endsWith("/..") || fileName.endsWith("/.")) {
            return false;
        }
        return true;
    }

    protected boolean isPositiveCommandCompletion() {
        int x = Client().getReplyCode();
        return x <= 300;
    }

    @Override
    public void login(final String userName, final String password) {
        user = userName;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(SOSVfs_D_132.params(user));
            }
            Client().login(user, password);
            logReply();
            if (ftpReply.isSuccessCode()) {
                commandListener.setClientId(getHostID(""));
                LOGGER.info(getHostID(SOSVfs_D_133.params(user)));
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
    public void logout() {
        try {
            if (Client().isConnected()) {
                Client().logout();
                LOGGER.debug(SOSVfs_D_138.params(host, getReplyString()));
            } else {
                LOGGER.info(SOSVfs_I_139.get());
            }
        } catch (IOException e) {
            LOGGER.warn(SOSVfs_W_140.get() + e.getMessage());
        }
    }

    protected boolean logReply() {
        reply = getReplyString();
        if (connectionOptionsAlternate.protocolCommandListener.isFalse()) {
            LOGGER.trace(reply);
        }
        return true;
    }

    @Override
    public ISOSVirtualFolder mkdir(final SOSFolderName folderName) {
        this.mkdir(folderName.getValue());
        return null;
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
                    Client().makeDirectory(subfolders[i]);
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
    public void openInputFile(final String fileName) {
        //
    }

    @Override
    public void openOutputFile(final String fileName) {
        //
    }

    @Override
    public final ISOSSession openSession(final ISOSShellOptions options) throws Exception {
        notImplemented();
        return null;
    }

    @Override
    public final int passive() {
        final String method = CLASS_NAME + "::passive";

        try {
            int i = Client().pasv();
            if (isPositiveCommandCompletion() == false) {
                throw new JobSchedulerException(getHostID(SOSVfs_E_0106.params("pasv", "", getReplyString())));
            } else {
                LOGGER.info(getHostID(SOSVfs_E_0106.params("pasv", "", getReplyString())));
                Client().enterLocalPassiveMode();
            }
            return i;
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    @Override
    public void put(final String localFile, final String remoteFile) {
        FileInputStream in = null;
        boolean rc = false;
        try {
            in = new FileInputStream(localFile);
            rc = Client().storeFile(remoteFile, in);
            if (!rc) {
                throw new JobSchedulerException(SOSVfs_E_142.params("put"));
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("put"), e);
        } finally {
            closeInput(in);
        }
    }

    @Override
    public void putFile(final ISOSVirtualFile file) {
        OutputStream os = null;
        InputStream is = null;
        try {
            os = getFileHandle(file.getName()).getFileOutputStream();
            is = file.getFileInputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            synchronized (this) {
                while ((bytes = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytes);
                }
                is.close();
                os.flush();
                os.close();

                is = null;
                os = null;
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("putfile"), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public long putFile(final String localFile, final OutputStream out) {
        if (out == null) {
            throw new JobSchedulerException(SOSVfs_E_147.get());
        }
        FileInputStream in = null;
        long total = 0;
        try {
            byte[] buffer = new byte[4096];
            in = new FileInputStream(new File(localFile));
            int bytes;
            synchronized (this) {
                while ((bytes = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes);
                    total += bytes;
                }
            }
            closeInput(in);
            closeObject(out);
            if (!Client().completePendingCommand()) {
                logout();
                disconnect();
                throw new JobSchedulerException(SOSVfs_E_134.params("File transfer") + SOSVfs_E_148.get());
            }
            if (isNegativeCommandCompletion()) {
                throw new JobSchedulerException(SOSVfs_E_144.params("getFile()", localFile, getReplyString()));
            }
            return total;
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("putfile"), e);
        } finally {
            closeInput(in);
            closeObject(out);
        }
    }

    @Override
    public long putFile(final String localFile, final String remoteFile) throws Exception {
        OutputStream os = Client().storeFileStream(remoteFile);
        if (isNegativeCommandCompletion()) {
            throw new JobSchedulerException(SOSVfs_E_144.params("storeFileStream()", remoteFile, getReplyString()));
        }
        long i = putFile(localFile, os);
        LOGGER.debug(SOSVfs_D_146.params(localFile, remoteFile));
        return i;
    }

    @Override
    public int read(final byte[] buffer) {
        return 0;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) {
        return 0;
    }

    @Override
    public boolean remoteIsWindowsShell() {
        return false;
    }

    @Override
    public void rename(final String from, final String to) {
        try {
            Client().rename(from, to);
            if (isNegativeCommandCompletion()) {
                throw new JobSchedulerException(SOSVfs_E_144.params("rename()", from, getReplyString()));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("rename()"), e);
        }
    }

    @Override
    public boolean rmdir(final SOSFolderName folderName) throws IOException {
        rmdir(folderName.getValue());
        return true;
    }

    @Override
    public final void rmdir(final String path) throws IOException {
        final String method = CLASS_NAME + "::rmdir";
        try {
            SOSOptionFolderName folderName = new SOSOptionFolderName(path);
            for (String folder : folderName.getSubFolderArrayReverse()) {
                String folderPath = folder + "/";
                Client().removeDirectory(folderPath);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(getHostID(SOSVfs_E_0106.params(method, folderPath, getReplyString())));
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    protected void sendCommand(final String command) {
        try {
            Client().sendCommand(command);
        } catch (IOException e) {
            throw new JobSchedulerException("command failed: " + command, e);
        }
        logReply();
    }

    @Override
    public void setConnected(final boolean val) {
        super.setConnected(val);
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities utilities) {
        //
    }

    @Override
    public void setLogin(final boolean val) {
        super.setLogin(val);
    }

    public long size(final String remoteFile) throws Exception {
        long size = -1L;
        if (transferMode.isAscii()) {
            this.binary();
        }
        Client().sendCommand("SIZE " + remoteFile);
        logReply();
        if (Client().getReplyCode() == FTPReply.FILE_STATUS) {
            size = Long.parseLong(trimResponseCode(getReplyString()));
        }
        if (transferMode.isAscii()) {
            this.ascii();
        }
        return size;
    }

    @Override
    public final ISOSVirtualFile transferMode(final SOSOptionTransferMode mode) {
        transferMode = mode;
        if (transferMode.isAscii()) {
            ascii();
        } else {
            binary();
        }
        LOGGER.info(SOSVfs_D_123.params(transferMode.getDescription(), getReplyString()));
        return null;
    }

    private String trimResponseCode(final String response) throws Exception {
        if (response.length() < 5) {
            return response;
        }
        return response.substring(4).trim();
    }

    @Override
    public void write(final byte[] buffer) {
        //
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        //
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

    protected boolean usingProxy() {
        return !SOSString.isEmpty(getProxyHost());
    }

    protected boolean usingHttpProxy() {
        return getProxyProtocol() != null && getProxyProtocol().isHttp();
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

    @Override
    public void reconnect(SOSConnection2OptionsAlternate options) {
        if (!isConnected()) {
            try {
                connect(options);
                authenticate(options);
                if (options.passiveMode.value()) {
                    passive();
                }
                if (options.transferMode.isDirty() && options.transferMode.isNotEmpty()) {
                    transferMode(options.transferMode);
                }
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Exception e) {
                throw new JobSchedulerException(e);
            }
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

    @Override
    public SOSCommandResult executePrivateCommand(String cmd) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SOSFileEntry> listNames(final String pathname, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException {
        return nList(pathname, false, checkIfExists);
    }

    @Override
    public List<SOSFileEntry> nList(final String pathname, final boolean recursive, boolean checkIfExists) {
        final String method = CLASS_NAME + "::nList";
        try {
            return getFilenames(pathname, recursive);
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ISOSConnectionOptions getConnectionOption() {
        return connectionOptions;
    }

    public SOSConnection2OptionsAlternate getConnectionOptionsAlternate() {
        return connectionOptionsAlternate;
    }

    public SOSFtpServerReply getFtpReply() {
        return ftpReply;
    }

    public void setCurrentPath(String val) {
        currentPath = val;
    }

    public SOSFtpClientLogger getCommandListener() {
        return commandListener;
    }

    public void setCommandListener(SOSFtpClientLogger val) {
        commandListener = val;
    }
}