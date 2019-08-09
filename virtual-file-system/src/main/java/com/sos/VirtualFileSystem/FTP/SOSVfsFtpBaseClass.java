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
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.Util;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.VirtualFileSystem.DataElements.SOSFileList;
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
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsSuperClass;
import com.sos.VirtualFileSystem.common.SOSCommandResult;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSVfsBaseClass;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSString;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtpBaseClass extends SOSVfsBaseClass implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection {

    protected Vector<String> vecDirectoryListing = null;
    protected ISOSConnectionOptions objConnectionOptions = null;
    protected SOSConnection2OptionsAlternate objConnection2Options = null;
    protected String strCurrentPath = EMPTY_STRING;
    protected String strReply = EMPTY_STRING;
    protected SOSFtpClientLogger objProtocolCommandListener = null;
    protected String host = EMPTY_STRING;
    protected int port = 0;
    protected String gstrUser = EMPTY_STRING;
    protected SOSOptionHostName objHost = null;
    protected SOSOptionPortNumber objPort = null;
    protected boolean dataChannelEncrypted = false;
    protected SOSFtpServerReply objFTPReply = null;
    protected SOSOptionTransferMode objTransferMode = null;
    private static final String CLASS_NAME = "SOSVfsFtpBaseClass";
    private static final Logger LOGGER = Logger.getLogger(SOSVfsFtpBaseClass.class);
    private ISOSAuthenticationOptions objAO = null;
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
        long i;
        try {
            i = putFile(localFile, Client().appendFileStream(remoteFile));
            LOGGER.info(SOSVfs_I_155.params(i));
        } catch (IOException e) {
            throw new RuntimeException(SOSVfs_E_130.params("appendFileStream", e));
        }
        return i;
    }

    @Override
    public void ascii() {
        try {
            boolean flgResult = Client().setFileType(FTP.ASCII_FILE_TYPE);
            if (!flgResult) {
                throw new JobSchedulerException(SOSVfs_E_149.params(getReplyString()));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("ascii", e));
        }
    }

    @Override
    public ISOSConnection authenticate(final ISOSAuthenticationOptions pobjAO) throws Exception {
        String user = pobjAO.getUser().getValue();
        String Passwd = pobjAO.getPassword().getValue();
        this.login(user, Passwd);
        objAO = pobjAO;
        return this;
    }

    @Override
    public void binary() {
        try {
            boolean flgResult = Client().setFileType(FTP.BINARY_FILE_TYPE);
            if (!flgResult) {
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
            LOGGER.debug(SOSVfs_D_135.params(pathname, getReplyString(), "[directory exists]"));
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
            if (objConnection2Options != null) {
                LOGGER.debug(SOSVfs_D_125.params(objConnection2Options.getHost().getValue()));
            }
            logReply();
        }
    }

    @Override
    public void closeInput() {
        //
    }

    private void closeInput(InputStream objO) {
        try {
            if (objO != null) {
                objO.close();
                objO = null;
                LOGGER.debug("InputStream closed");
            }
        } catch (IOException e) {
            //
        }
    }

    private void closeObject(OutputStream objO) {
        try {
            if (objO != null) {
                objO.flush();
                objO.close();
                objO = null;
                LOGGER.debug("OutputStream closed");
            }
        } catch (Exception e) {
            //
        }
    }

    @Override
    public void closeOutput() {
        final String conMethodName = CLASS_NAME + "::closeOutput";
        try {
            this.completePendingCommand();
        } catch (Exception e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
    }

    @Override
    public void closeSession() throws Exception {
        this.logout();
    }

    @Override
    public void completePendingCommand() {
        final String conMethodName = CLASS_NAME + "::CompletePendingCommand";
        LOGGER.trace("completePendingCommand");
        try {
            if (!Client().completePendingCommand()) {
                logout();
                disconnect();
                raiseException(getHostID(SOSVfs_E_0105.params(conMethodName)));
            }
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        if (isNegativeCommandCompletion()) {
            throw new JobSchedulerException(SOSVfs_E_124.params(getReplyString()));
        }
    }

    public void doConnect(final String phost, final int pport) {
        try {
            host = phost;
            port = pport;
            String strM = SOSVfs_D_0101.params(host, port);
            LOGGER.debug(strM);
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
    public ISOSConnection connect(final String pstrHostName, final int pintPortNumber) throws Exception {
        this.doConnect(pstrHostName, pintPortNumber);
        if (objConnectionOptions != null) {
            objConnectionOptions.getHost().setValue(pstrHostName);
            objConnectionOptions.getPort().value(pintPortNumber);
        }
        return this;
    }

    @Override
    public ISOSConnection connect() {
        final String conMethodName = CLASS_NAME + "::Connect";
        String strH = host = objConnectionOptions.getHost().getValue();
        int intP = port = objConnectionOptions.getPort().value();
        LOGGER.debug(SOSVfs_D_0101.params(strH, intP));
        try {
            if (objConnection2Options != null) {
                proxyProtocol = objConnection2Options.proxyProtocol;
                proxyHost = objConnection2Options.proxyHost.getValue();
                proxyPort = objConnection2Options.proxyPort.value();
                proxyUser = objConnection2Options.proxyUser.getValue();
                proxyPassword = objConnection2Options.proxyPassword.getValue();
            }
            this.doConnect(strH, intP);
            LOGGER.info(SOSVfs_D_0102.params(strH, intP));
            Client().setControlKeepAliveTimeout(180);
        } catch (RuntimeException e) {
            LOGGER.info(SOSVfs_E_0107.params(host, port, e.getMessage()));
            if (objConnectionOptions.getAlternativeHost().isDirty()) {
                String strAltHost = host = objConnectionOptions.getAlternativeHost().getValue();
                int intAltPort = port = objConnectionOptions.getAlternativePort().value();
                if (isNotEmpty(strAltHost) && intAltPort > 0) {
                    try {
                        JobSchedulerException.gflgStackTracePrinted = false;
                        this.connect(strAltHost, intAltPort);
                    } catch (Exception e1) {
                        LOGGER.info(SOSVfs_E_0107.params(host, port, e1.getMessage()));
                    }
                    LOGGER.info(SOSVfs_D_0102.params(strAltHost, intAltPort));
                } else {
                    LOGGER.info(SOSVfs_E_0107.params(host, port, e.getMessage()));
                    raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
                }
            } else {
                LOGGER.info(SOSVfs_E_0107.params(host, port, e.getMessage()));
                throw e;
            }
        }
        return this;
    }

    @Deprecated
    @Override
    public ISOSConnection connect(final ISOSConnectionOptions pobjConnectionOptions) throws Exception {
        try {
            objConnectionOptions = pobjConnectionOptions;
            this.connect();
        } catch (Exception e) {
            throw e;
        }
        return this;
    }

    @Override
    public ISOSConnection connect(final SOSConnection2OptionsAlternate pobjConnectionOptions) {
        final String conMethodName = CLASS_NAME + "::Connect";
        objConnection2Options = pobjConnectionOptions;
        try {
            objHost = objConnection2Options.getHost();
            objPort = objConnection2Options.getPort();
            proxyProtocol = objConnection2Options.proxyProtocol;
            proxyHost = objConnection2Options.proxyHost.getValue();
            proxyPort = objConnection2Options.proxyPort.value();
            proxyUser = objConnection2Options.proxyUser.getValue();
            proxyPassword = objConnection2Options.proxyPassword.getValue();
            this.connect(objHost.getValue(), objPort.value());
            if (!Client().isConnected()) {
                SOSConnection2OptionsSuperClass objAlternate = objConnection2Options.getAlternatives();
                objHost = objAlternate.host;
                objPort = objAlternate.port;
                proxyProtocol = objAlternate.proxyProtocol;
                proxyHost = objAlternate.proxyHost.getValue();
                proxyPort = objAlternate.proxyPort.value();
                proxyUser = objAlternate.proxyUser.getValue();
                proxyPassword = objAlternate.proxyPassword.getValue();
                LOGGER.info(SOSVfs_I_0121.params(host));
                this.connect(objHost.getValue(), objPort.value());
                if (!Client().isConnected()) {
                    objHost = null;
                    objPort = null;
                    host = "";
                    port = -1;
                    raiseException(SOSVfs_E_204.get());
                }
            }
        } catch (Exception e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return this;
    }

    @Override
    public ISOSConnection connect(final ISOSDataProviderOptions pobjConnectionOptions) throws Exception {
        return null;
    }

    @Override
    public String createScriptFile(final String pstrContent) throws Exception {
        notImplemented();
        return null;
    }

    @Override
    public void delete(final String pathname) throws IOException {
        try {
            Client().deleteFile(pathname);
            if (isNegativeCommandCompletion()) {
                raiseException(SOSVfs_E_144.params("delete()", pathname, getReplyString()));
            } else {
                LOGGER.info(SOSVfs_I_131.params(pathname, getReplyString()));
            }
        } catch (IOException e) {
            raiseException(e, SOSVfs_E_134.params("delete()"));
        }
    }

    public SOSFileList dir() {
        try {
            return dir(".");
        } catch (Exception e) {
            throw new RuntimeException(SOSVfs_E_130.params("dir"), e);
        }
    }

    @Override
    public SOSFileList dir(final SOSFolderName pobjFolderName) {
        this.dir(pobjFolderName.getValue());
        return null;
    }

    public SOSFileList dir(final String pathname) {
        Vector<String> strList = getFilenames(pathname);
        String[] strT = strList.toArray(new String[strList.size()]);
        SOSFileList objFileList = new SOSFileList(strT);
        return objFileList;
    }

    @Override
    public SOSFileList dir(final String pathname, final int flag) {
        SOSFileList fileList = new SOSFileList();
        FTPFile[] listFiles;
        try {
            listFiles = Client().listFiles(pathname);
        } catch (IOException e) {
            throw new RuntimeException(SOSVfs_E_128.params("listfiles", "dir"), e);
        }
        for (FTPFile listFile : listFiles) {
            if (flag > 0 && listFile.isDirectory()) {
                fileList.addAll(this.dir(pathname + "/" + listFile.toString(), flag >= 1024 ? flag : flag + 1024));
            } else {
                if (flag >= 1024) {
                    fileList.add(pathname + "/" + listFile.toString());
                } else {
                    fileList.add(listFile.toString());
                }
            }
        }
        return fileList;
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

    private int doCD(final String strFolderName) {
        int x = 0;
        try {
            String strT = strFolderName.replaceAll("\\\\", "/");
            LOGGER.debug(SOSVfs_D_127.params(strT));
            x = cd(strT);
            logReply();
        } catch (IOException e) {
            //
        }
        return x;
    }

    protected int doCDUP() {
        final String conMethodName = CLASS_NAME + "::DoCDUP";
        try {
            LOGGER.debug(SOSVfs_D_141.params("cdup"));
            Client().cdup();
            logReply();
            doPWD();
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return 0;
    }

    @Override
    public void doPostLoginOperations() {
        Client().setControlKeepAliveTimeout(180);
        String strT;
        try {
            strT = Client().getSystemType();
            LOGGER.debug(String.format("System-Type = %1$s", strT));
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
        sendCommand("FEAT");
        if (objFTPReply.getCode() == FTPReply.SYSTEM_STATUS) {
            String[] lines = objFTPReply.getMessages();
            for (int i = 1; i < lines.length - 1; i++) {
                String feat = lines[i].trim().toUpperCase();
                if ("UTF8".equals(feat)) {
                    Client().setControlEncoding("UTF-8");
                    break;
                }
            }
        } else {
            LOGGER.info("no valid response for FEAT command received: " + objFTPReply.toString());
        }
        sendCommand("NOOP");
    }

    @Override
    public final String doPWD() {
        final String conMethodName = CLASS_NAME + "::DoPWD";
        String lstrCurrentPath = "";
        try {
            LOGGER.debug(SOSVfs_D_141.params("pwd"));
            lstrCurrentPath = getCurrentPath();
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return lstrCurrentPath;
    }

    @Override
    public void executeCommand(final String strCmd) throws Exception {
        executeCommand(strCmd, null);
    }

    @Override
    public void executeCommand(final String strCmd, SOSVfsEnv env) throws Exception {
        String command = strCmd.endsWith("\n") ? strCmd : strCmd + "\n";
        sendCommand(command);
        LOGGER.info(SOSVfs_D_151.params(strCmd, getReplyString()));
        Client().completePendingCommand();
    }

    @Override
    public void flush() {
        //
    }

    public void get(final String remoteFile, final String localFile) {
        FileOutputStream out = null;
        boolean rc = false;
        try {
            out = new FileOutputStream(localFile);
            rc = Client().retrieveFile(remoteFile, out);
            if (!rc) {
                throw new JobSchedulerException(SOSVfs_E_142.get());
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("get"), e);
        } finally {
            closeObject(out);
        }
    }

    @Override
    public OutputStream getAppendFileStream(final String strFileName) {
        final String conMethodName = CLASS_NAME + "::getAppendFileStream";
        OutputStream objO = null;
        try {
            objO = Client().appendFileStream(strFileName);
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return objO;
    }

    @Override
    public ISOSConnection getConnection() {
        return this;
    }

    protected final String getCurrentPath() {
        final String conMethodName = CLASS_NAME + "::getCurrentPath";
        String lstrCurrentPath = strCurrentPath;
        try {
            Client().pwd();
            lstrCurrentPath = getReplyString();
            LOGGER.debug(getHostID(SOSVfs_E_0106.params(conMethodName, "", lstrCurrentPath)));
            lstrCurrentPath = lstrCurrentPath.replaceFirst("^[^\"]*\"([^\"]*)\".*", "$1");
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return lstrCurrentPath;
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
        final boolean flgAppendLocalFile = false;
        return this.getFile(remoteFile, localFile, flgAppendLocalFile);
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
            raiseException(e, SOSVfs_E_130.params("getFile"));
        } finally {
            closeInput(in);
            closeObject(out);
        }
        return totalBytes;
    }

    @Override
    public ISOSVirtualFile getFileHandle(final String pstrFilename) {
        ISOSVirtualFile objFtpFile = new SOSVfsFtpFile(pstrFilename);
        objFtpFile.setHandler(this);
        return objFtpFile;
    }

    @Override
    public String[] getFilelist(final String folder, final String regexp, final int flag, final boolean withSubFolder, String integrityHashType) {
        vecDirectoryListing = nList(folder, withSubFolder);
        Vector<String> strB = new Vector<String>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (String strFile : vecDirectoryListing) {
            String strFileName = new File(strFile).getName();
            // file list should not contain the checksum files
            if (integrityHashType != null && strFileName.endsWith(integrityHashType)) {
                continue;
            }
            Matcher matcher = pattern.matcher(strFileName);
            if (matcher.find()) {
                strB.add(strFile);
            }
        }
        return strB.toArray(new String[strB.size()]);
    }

    private Vector<String> getFilenames() throws Exception {
        return getFilenames("", false);
    }

    private Vector<String> getFilenames(final boolean flgRecurseSubFolders) throws Exception {
        return getFilenames("", flgRecurseSubFolders);
    }

    private Vector<String> getFilenames(final String pathname) {
        return getFilenames(pathname, false);
    }

    private Vector<String> getFilenames(final String pathName, final boolean recurseSubFolders) {
        Vector<String> listOfFilenames = getFilenames(pathName, recurseSubFolders, true);

        for (String currentFileName : listOfFilenames) {
            SOSFileEntry sosFileEntry = new SOSFileEntry();
            sosFileEntry.setFilename(currentFileName);
            sosFileEntries.add(sosFileEntry);
        }
        return getFilenames(pathName, recurseSubFolders, true);
    }

    private Vector<String> getFilenames(final String path, final boolean withRecurseSubFolders, final boolean checkReplyCode) {
        Vector<String> dirListing = new Vector<String>();
        FTPFile[] ftpFileList = null;
        String pathName = path.trim();
        if (pathName == null) {
            pathName = "";
        }
        if (pathName.isEmpty()) {
            pathName = ".";
        }
        try {
            ftpFileList = Client().listFiles(pathName);
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params("getFilenames")));
        }
        if (ftpFileList == null || ftpFileList.length <= 0) {
            if (isNegativeCommandCompletion()) {
                String message = getHostID(SOSVfs_E_0105.params("getFilenames")) + ":" + getReplyString();
                if (checkReplyCode) {
                    raiseException(message);
                } else {
                    LOGGER.warn(message);
                }
            }
            return dirListing;
        }
        for (FTPFile ftpFile : ftpFileList) {
            String currentFile = ftpFile.getName();
            if (isNotHiddenFile(currentFile)) {
                if (currentFile.indexOf("/") == -1) {
                    currentFile = pathName + "/" + currentFile;
                    currentFile = currentFile.replaceAll("//+", "/");
                }
                if (ftpFile.isFile()) {
                    dirListing.add(currentFile);
                } else if (ftpFile.isDirectory() && withRecurseSubFolders) {
                    Vector<String> filelist = getFilenames(currentFile + "/", withRecurseSubFolders);
                    if (filelist != null && !filelist.isEmpty()) {
                        dirListing.addAll(filelist);
                    }
                }
            }
        }
        return dirListing;
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
    public long getFileSize(final String strFileName) {
        final String conMethodName = CLASS_NAME + "::getFileSize";
        long lngFileSize = -1;
        try {
            lngFileSize = size(strFileName);
        } catch (Exception e) {
            raiseException(SOSVfs_E_153.params(conMethodName, e));
        }
        return lngFileSize;
    }

    @Override
    public String[] getFolderlist(final String folder, final String regexp, final int flag, final boolean withSubFolder) {
        vecDirectoryListing = null;
        if (vecDirectoryListing == null) {
            vecDirectoryListing = nList(folder, withSubFolder);
        }
        Vector<String> strB = new Vector<String>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (String strFile : vecDirectoryListing) {
            String strFileName = new File(strFile).getName();
            Matcher matcher = pattern.matcher(strFileName);
            if (matcher.find()) {
                strB.add(strFile);
            }
        }
        return strB.toArray(new String[strB.size()]);
    }

    protected FTPFile getFTPFile(final String strFileName) {
        final String conMethodName = CLASS_NAME + "::getFTPFile";
        FTPFile objFTPFile = null;
        try {
            FTPFile[] ftpFiles = Client().listFiles(strFileName);
            if (isNotNull(ftpFiles) && ftpFiles.length > 0) {
                objFTPFile = ftpFiles[0];
            }
        } catch (Exception e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return objFTPFile;
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
    public InputStream getInputStream(String strFileName) {
        final String conMethodName = CLASS_NAME + "::getInputStream";
        InputStream objI = null;
        try {
            strFileName = strFileName.replaceAll("\\\\", "/");
            objI = Client().retrieveFileStream(strFileName);
            if (objI == null) {
                throw new JobSchedulerException(String.format("unable to get inputstream for file '%1$s'", strFileName));
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        } finally {
            logReply();
        }
        return objI;
    }

    @Override
    public String getModificationTime(final String strFileName) {
        final String conMethodName = CLASS_NAME + "::getModificationTime";
        String strT = null;
        try {
            strT = Client().getModificationTime(strFileName);
        } catch (Exception e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return strT;
    }

    public SOSFileListEntry getNewVirtualFile(final String pstrFileName) {
        SOSFileListEntry objF = new SOSFileListEntry(pstrFileName);
        objF.setVfsHandler(this);
        return objF;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public OutputStream getOutputStream(final String strFileName) {
        OutputStream objO = null;
        try {
            objO = Client().storeFileStream(strFileName);
            logReply();
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params("getOutputStream", e.getMessage())), e);
        }
        return objO;
    }

    @Override
    public final String getReplyString() {
        String strT = Client().getReplyString();
        if (strT != null) {
            strT = strT.trim();
        }
        objFTPReply = new SOSFtpServerReply(strT);
        return strT;
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

    protected final String getHostID(final String pstrText) {
        return "(" + gstrUser + "@" + host + ":" + port + ") " + pstrText;
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
    public final boolean isDirectory(final String pstrPathName) {
        boolean flgResult = false;
        if (isNotHiddenFile(pstrPathName)) {
            if (strCurrentPath.isEmpty()) {
                strCurrentPath = getCurrentPath();
            }
            if (strCurrentPath.replaceFirst("/$", "").equals(pstrPathName.replaceFirst("/$", ""))) {
                flgResult = true;
            } else {
                doCD(pstrPathName);
                if (isPositiveCommandCompletion()) {
                    doCD(strCurrentPath);
                    flgResult = true;
                }
            }
        }
        return flgResult;
    }

    @Override
    public boolean isLoggedin() {
        return flgLoggedIn;
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
    public String[] listNames(final String pathname) throws IOException {
        String strA[] = Client().listNames(pathname);
        for (int i = 0; i < strA.length; i++) {
            strA[i] = strA[i].replaceAll("\\\\", "/");
        }
        LOGGER.debug(SOSVfs_D_137.params(Client().getReplyString(), Client().getReplyCode()));
        return strA;
    }

    @Override
    public void login(final String strUserName, final String strPassword) {
        gstrUser = strUserName;
        try {
            LOGGER.debug(SOSVfs_D_132.params(strUserName));
            Client().login(strUserName, strPassword);
            logReply();
            if (objFTPReply.isSuccessCode()) {
                LOGGER.debug(SOSVfs_D_132.params(strUserName));
                objProtocolCommandListener.setClientId(getHostID(""));
                LOGGER.info(getHostID(SOSVfs_D_133.params(strUserName)));
                try {
                    doPostLoginOperations();
                } catch (Exception e) {
                    //
                }
            } else {
                LOGGER.info(SOSVfs_D_132.params(strUserName));
                throw new JobSchedulerException(SOSVfs_E_134.params("Login") + " code:" + objFTPReply.getCode() + " Message: " + objFTPReply
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
            if (this.Client().isConnected()) {
                this.Client().logout();
                String strHost = host;
                if (objHost != null) {
                    strHost = objHost.getValue();
                }
                LOGGER.debug(SOSVfs_D_138.params(strHost, getReplyString()));
            } else {
                LOGGER.info(SOSVfs_I_139.get());
            }
        } catch (IOException e) {
            LOGGER.warn(SOSVfs_W_140.get() + e.getMessage());
        }
    }

    protected boolean logReply() {
        strReply = getReplyString();
        if (objConnection2Options.protocolCommandListener.isFalse()) {
            LOGGER.trace(strReply);
        }
        return true;
    }

    @Override
    public ISOSVirtualFolder mkdir(final SOSFolderName pobjFolderName) {
        this.mkdir(pobjFolderName.getValue());
        return null;
    }

    @Override
    public void mkdir(final String pstrPathName) {
        final String conMethodName = CLASS_NAME + "::mkdir";
        try {
            SOSOptionFolderName objF = new SOSOptionFolderName(pstrPathName);
            LOGGER.debug(getHostID(SOSVfs_D_179.params("mkdir", pstrPathName)));
            String[] subfolders = objF.getSubFolderArrayReverse();
            int idx = subfolders.length;
            for (String strSubFolder : objF.getSubFolderArrayReverse()) {
                if (isDirectory(strSubFolder)) {
                    LOGGER.debug(SOSVfs_E_180.params(strSubFolder));
                    break;
                }
                idx--;
            }
            subfolders = objF.getSubFolderArray();
            for (int i = idx; i < subfolders.length; i++) {
                Client().makeDirectory(subfolders[i]);
                LOGGER.debug(getHostID(SOSVfs_E_0106.params(conMethodName, subfolders[i], getReplyString())));
            }
        } catch (IOException e) {
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
    }

    @Override
    public Vector<String> nList() throws Exception {
        return getFilenames();
    }

    @Override
    public Vector<String> nList(final boolean recursive) throws Exception {
        return getFilenames(recursive);
    }

    @Override
    public Vector<String> nList(final String pathname) {
        return getFilenames(pathname);
    }

    @Override
    public Vector<String> nList(final String pathname, final boolean flgRecurseSubFolder) {
        try {
            return getFilenames(pathname, flgRecurseSubFolder);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_128.params("getfilenames", "nList"), e);
        }
    }

    @Override
    public void openInputFile(final String pstrFileName) {
        //
    }

    @Override
    public void openOutputFile(final String pstrFileName) {
        //
    }

    @Override
    public final ISOSSession openSession(final ISOSShellOptions pobjShellOptions) throws Exception {
        notImplemented();
        return null;
    }

    @Override
    public final int passive() {
        final String conMethodName = CLASS_NAME + "::passive";
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
            raiseException(e, getHostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return 0;
    }

    @Override
    public void put(final String localFile, final String remoteFile) {
        FileInputStream in = null;
        boolean rc = false;
        try {
            in = new FileInputStream(localFile);
            rc = Client().storeFile(remoteFile, in);
            if (!rc) {
                raiseException(SOSVfs_E_142.params("put"));
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_130.params("put"));
        } finally {
            closeInput(in);
        }
    }

    @Override
    public void putFile(final ISOSVirtualFile objVirtualFile) {
        String strName = objVirtualFile.getName();
        ISOSVirtualFile objVF = this.getFileHandle(strName);
        OutputStream objOS = objVF.getFileOutputStream();
        InputStream objFI = objVirtualFile.getFileInputStream();
        int lngBufferSize = 1024;
        byte[] buffer = new byte[lngBufferSize];
        int intBytesTransferred;
        long totalBytes = 0;
        try {
            synchronized (this) {
                while ((intBytesTransferred = objFI.read(buffer)) != -1) {
                    objOS.write(buffer, 0, intBytesTransferred);
                    totalBytes += intBytesTransferred;
                }
                objFI.close();
                objOS.flush();
                objOS.close();
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("putfile"), e);
        }
    }

    @Override
    public long putFile(final String localFile, final OutputStream out) {
        if (out == null) {
            raiseException(SOSVfs_E_147.get());
        }
        FileInputStream in = null;
        long lngTotalBytesWritten = 0;
        try {
            byte[] buffer = new byte[4096];
            in = new FileInputStream(new File(localFile));
            int bytesWritten;
            synchronized (this) {
                while ((bytesWritten = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesWritten);
                    lngTotalBytesWritten += bytesWritten;
                }
            }
            closeInput(in);
            closeObject(out);
            if (!Client().completePendingCommand()) {
                logout();
                disconnect();
                raiseException(SOSVfs_E_134.params("File transfer") + SOSVfs_E_148.get());
            }
            if (isNegativeCommandCompletion()) {
                raiseException(SOSVfs_E_144.params("getFile()", localFile, getReplyString()));
            }
            return lngTotalBytesWritten;
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_130.params("putfile"));
        } finally {
            closeInput(in);
            closeObject(out);
        }
        return lngTotalBytesWritten;
    }

    @Override
    public long putFile(final String localFile, final String remoteFile) throws Exception {
        OutputStream outputStream = Client().storeFileStream(remoteFile);
        if (isNegativeCommandCompletion()) {
            raiseException(SOSVfs_E_144.params("storeFileStream()", remoteFile, getReplyString()));
        }
        long i = putFile(localFile, outputStream);
        LOGGER.debug(SOSVfs_D_146.params(localFile, remoteFile));
        return i;
    }

    protected void raiseException(final Exception e, final String pstrM) {
        LOGGER.error(pstrM + " (" + e.getMessage() + ")");
        throw new JobSchedulerException(pstrM, e);
    }

    protected void raiseException(final String pstrM) {
        LOGGER.error(pstrM);
        throw new JobSchedulerException(pstrM);
    }

    @Override
    public int read(final byte[] bteBuffer) {
        return 0;
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        return 0;
    }

    @Override
    public boolean remoteIsWindowsShell() {
        return false;
    }

    @Override
    public void rename(final String from, final String to) {
        try {
            this.Client().rename(from, to);
            if (isNegativeCommandCompletion()) {
                raiseException(SOSVfs_E_144.params("rename()", from, getReplyString()));
            } else {
                LOGGER.info(String.format(SOSVfs_I_150.params(from, to)));
            }
        } catch (IOException e) {
            raiseException(e, SOSVfs_E_134.params("rename()"));
        }
    }

    @Override
    public boolean rmdir(final SOSFolderName pobjFolderName) throws IOException {
        this.rmdir(pobjFolderName.getValue());
        return true;
    }

    @Override
    public final void rmdir(final String pstrPathName) throws IOException {
        final String conMethodName = CLASS_NAME + "::rmdir";
        try {
            SOSOptionFolderName objF = new SOSOptionFolderName(pstrPathName);
            for (String subfolder : objF.getSubFolderArrayReverse()) {
                String strT = subfolder + "/";
                Client().removeDirectory(strT);
                LOGGER.debug(getHostID(SOSVfs_E_0106.params(conMethodName, strT, getReplyString())));
            }
        } catch (Exception e) {
            String strM = getHostID(SOSVfs_E_0105.params(conMethodName));
            throw new JobSchedulerException(strM, e);
        }
    }

    protected void sendCommand(final String pstrCommand) {
        try {
            Client().sendCommand(pstrCommand);
        } catch (IOException e) {
            throw new JobSchedulerException("command failed: " + pstrCommand, e);
        }
        logReply();
    }

    @Override
    public void setConnected(final boolean pflgIsConnected) {
        flgConnected = pflgIsConnected;
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        //
    }

    @Override
    public void setLogin(final boolean pflgIsLogin) {
        flgLoggedIn = pflgIsLogin;
    }

    public long size(final String remoteFile) throws Exception {
        long lngFileSize = -1L;
        if (objTransferMode.isAscii()) {
            this.binary();
        }
        Client().sendCommand("SIZE " + remoteFile);
        logReply();
        if (Client().getReplyCode() == FTPReply.FILE_STATUS) {
            lngFileSize = Long.parseLong(trimResponseCode(this.getReplyString()));
        }
        if (objTransferMode.isAscii()) {
            this.ascii();
        }
        return lngFileSize;
    }

    @Override
    public final ISOSVirtualFile transferMode(final SOSOptionTransferMode pobjFileTransferMode) {
        objTransferMode = pobjFileTransferMode;
        String strMode = pobjFileTransferMode.getDescription();
        if (pobjFileTransferMode.isAscii()) {
            this.ascii();
        } else {
            this.binary();
        }
        LOGGER.debug(SOSVfs_D_122.params(strMode));
        LOGGER.info(SOSVfs_D_123.params(strMode, getReplyString()));
        return null;
    }

    private String trimResponseCode(final String response) throws Exception {
        if (response.length() < 5) {
            return response;
        }
        return response.substring(4).trim();
    }

    @Override
    public void write(final byte[] bteBuffer) {
        //
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        //
    }

    @Override
    public SOSFileEntries getSOSFileEntries() {
        return sosFileEntries;
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
        return this.simulateShell;
    }

    @Override
    public void setSimulateShell(boolean simulateShell) {
        this.simulateShell = simulateShell;
    }

    @Override
    public SOSCommandResult executePrivateCommand(String cmd) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}