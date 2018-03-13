package com.sos.VirtualFileSystem.SFTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
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
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSVfsBaseClass;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3DirectoryEntry;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;
import com.trilead.ssh2.channel.Channel;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsSFtp extends SOSVfsBaseClass implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection {

    protected String authenticationFilename = "";
    protected Connection sshConnection = null;
    protected Session sshSession = null;
    protected SFTPv3Client objFTPClient = null;
    protected boolean isConnected = false;
    protected String reply = "OK";
    private static final Logger LOGGER = Logger.getLogger(SOSVfsSFtp.class);
    private Vector<String> vecDirectoryListing = null;
    private ISOSConnectionOptions objConnectionOptions = null;
    private SOSConnection2OptionsAlternate objConnection2Options = null;
    private final String strCurrentPath = EMPTY_STRING;
    private String host = EMPTY_STRING;
    private int port = 0;
    private String gstrUser = EMPTY_STRING;
    private SOSOptionHostName objHost = null;
    private SOSOptionPortNumber objPort = null;
    private String currentDirectory = "";
    private final char[] authenticationFile = null;
    private boolean simulateShell = false;
    private ISOSAuthenticationOptions objAO = null;
    private SFTPv3FileHandle objInputFile = null;
    private long lngReadOffset = 0;
    private SFTPv3FileHandle objOutputFile = null;
    private long lngWriteOffset = 0;

    public SOSVfsSFtp() {
        super();
    }

    private String getHostID(final String pstrText) {
        return "(" + gstrUser + "@" + host + ":" + port + ") " + pstrText;
    }

    public void doConnect(final String phost, final int pport) {
        try {
            host = phost;
            port = pport;
            LOGGER.debug(SOSVfs_D_0101.params(host, port));
            if (!isConnected()) {
                sshConnection = new Connection(host, port);
                sshConnection.connect();
                isConnected = true;
                LOGGER.info(SOSVfs_D_0102.params(host, port));
                logReply();
            } else {
                LOGGER.warn(SOSVfs_D_0103.params(host, port));
            }
        } catch (Exception e) {
            String strM = getHostID(SOSVfs_E_130.params("connect()"));
            LOGGER.error(strM, e);
        }
    }

    public SFTPv3FileAttributes getAttributes(final String filename) {
        SFTPv3FileAttributes attributes = null;
        try {
            attributes = this.getClient().lstat(filename);
        } catch (Exception e) {
            attributes = null;
        }
        return attributes;
    }

    @Override
    public void mkdir(final String pstrPathName) {
        try {
            SOSOptionFolderName objF = new SOSOptionFolderName(pstrPathName);
            reply = "mkdir OK";
            LOGGER.debug(getHostID(SOSVfs_D_179.params("mkdir", pstrPathName)));
            String[] subfolders = objF.getSubFolderArrayReverse();
            int idx = subfolders.length;
            for (String strSubFolder : objF.getSubFolderArrayReverse()) {
                SFTPv3FileAttributes attributes = getAttributes(strSubFolder);
                if (attributes != null && attributes.isDirectory()) {
                    LOGGER.debug(SOSVfs_E_180.params(strSubFolder));
                    break;
                }
                if (attributes != null && !attributes.isDirectory()) {
                    raiseException(SOSVfs_E_277.params(strSubFolder));
                    break;
                }
                idx--;
            }
            subfolders = objF.getSubFolderArray();
            for (int i = idx; i < subfolders.length; i++) {
                getClient().mkdir(subfolders[i], 484);
                LOGGER.debug(getHostID(SOSVfs_E_0106.params("mkdir", subfolders[i], getReplyString())));
            }
            LOGGER.debug(getHostID(SOSVfs_D_181.params("mkdir", pstrPathName, getReplyString())));
        } catch (IOException e) {
            reply = e.toString();
            raiseException(e, SOSVfs_E_134.params("[mkdir]"));
        }
    }

    @Override
    public void rmdir(final String pstrPathName) {
        try {
            SOSOptionFolderName objF = new SOSOptionFolderName(pstrPathName);
            reply = "rmdir OK";
            for (String subfolder : objF.getSubFolderArrayReverse()) {
                String strT = subfolder + "/";
                LOGGER.debug(getHostID(SOSVfs_D_135.params("[rmdir]", subfolder, getReplyString())));
                getClient().rmdir(strT);
            }
            reply = "rmdir OK";
        } catch (IOException e) {
            reply = e.toString();
            raiseException(e, SOSVfs_E_134.params("[rmdir]"));
        }
    }

    @Override
    public int passive() {
        return 0;
    }

    @Override
    public Vector<String> nList(final String pathname) {
        return getFilenames(pathname);
    }

    private Vector<String> getFilenames(final String pathname) {
        Vector<String> vecT;
        try {
            vecT = getFilenames(pathname, false);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("getFileNames()"), e);
        }
        return vecT;
    }

    private Vector<String> getFilenames(final String pstrPathName, final boolean flgRecurseSubFolders) throws Exception {
        String strCurrentDirectory = null;
        Vector<String> vecDirectoryListing = null;
        if (vecDirectoryListing == null) {
            vecDirectoryListing = new Vector<String>();
            String[] fileList = null;
            strCurrentDirectory = doPWD();
            String lstrPathName = pstrPathName.trim();
            if (lstrPathName.isEmpty()) {
                lstrPathName = ".";
            }
            if (".".equals(lstrPathName)) {
                lstrPathName = strCurrentDirectory;
            }
            try {
                fileList = listNames(lstrPathName);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
            if (fileList == null) {
                return vecDirectoryListing;
            }
            for (String strCurrentFile : fileList) {
                if (isNotHiddenFile(strCurrentFile)) {
                    if (!getClient().lstat(strCurrentFile).isDirectory()) {
                        if (!lstrPathName.startsWith("/") && !strCurrentFile.startsWith(strCurrentDirectory)) {
                            strCurrentFile = addFileSeparator(strCurrentDirectory) + strCurrentFile;
                        }
                        vecDirectoryListing.add(strCurrentFile);
                    } else {
                        if (flgRecurseSubFolders) {
                            LOGGER.debug(String.format("start scan for subdirectory '%1$s' ", strCurrentFile));
                            Vector<String> vecNames = getFilenames(strCurrentFile, flgRecurseSubFolders);
                            if (vecNames != null) {
                                vecDirectoryListing.addAll(vecNames);
                            }
                        }
                    }
                }
            }
        }
        LOGGER.debug(SOSVfs_I_126.params(strCurrentDirectory));
        if (strCurrentDirectory != null) {
            doCD(strCurrentDirectory);
            doPWD();
        }
        return vecDirectoryListing;
    }

    @Override
    public String doPWD() {
        String lstrCurrentPath = "";
        try {
            lstrCurrentPath = getCurrentPath();
        } catch (Exception e) {
            LOGGER.error(getHostID(SOSVfs_E_153.params("pwd.")), e);
            throw new JobSchedulerException(e);
        }
        return lstrCurrentPath;
    }

    private String getCurrentPath() {
        return strCurrentPath;
    }

    private int doCD(final String strFolderName) {
        int x = 0;
        try {
            x = cd(strFolderName);
            logReply();
        } catch (IOException e) {
            //
        }
        return x;
    }

    private boolean logReply() {
        reply = getReplyString();
        if (!reply.trim().isEmpty()) {
            LOGGER.debug(reply);
        }
        return true;
    }

    @Override
    public boolean isNegativeCommandCompletion() {
        int x = 0;
        return x > 300;
    }

    @Override
    public void completePendingCommand() {
        //
    }

    public boolean isNotHiddenFile(final String strFileName) {
        return !isHiddenFile(strFileName);
    }

    private boolean isHiddenFile(final String fileName) {
        boolean flgR = false;
        if (fileName == null || ".".equals(fileName) || "..".equals(fileName) || fileName.endsWith("/..") || fileName.endsWith("/.")) {
            flgR = true;
        }
        return flgR;
    }

    @Override
    public Vector<String> nList(final String pathname, final boolean flgRecurseSubFolder) {
        try {
            return getFilenames(pathname, flgRecurseSubFolder);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_128.params("getfilenames()", "nList"), e);
        }
    }

    @Override
    public Vector<String> nList() throws Exception {
        return getFilenames();
    }

    private Vector<String> getFilenames() throws Exception {
        return getFilenames("", false);
    }

    private Vector<String> getFilenames(final boolean flgRecurseSubFolders) throws Exception {
        return getFilenames("", flgRecurseSubFolders);
    }

    @Override
    public Vector<String> nList(final boolean recursive) throws Exception {
        return getFilenames(recursive);
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
        String[] listFiles = null;
        try {
            listFiles = this.listNames(pathname);
        } catch (IOException e) {
            raiseException(e, SOSVfs_E_128.params("listfiles()", "dir"));
        }
        if (listFiles != null) {
            for (String listFile : listFiles) {
                if (flag > 0 && isDirectory(listFile)) {
                    fileList.addAll(this.dir(pathname + "/" + listFile, flag >= 1024 ? flag : flag + 1024));
                } else {
                    if (flag >= 1024) {
                        fileList.add(pathname + "/" + listFile.toString());
                    } else {
                        fileList.add(listFile.toString());
                    }
                }
            }
        }
        return fileList;
    }

    @Override
    public boolean isDirectory(final String filename) {
        try {
            return getClient().stat(filename).isDirectory();
        } catch (Exception e) {
            //
        }
        return false;
    }

    @Override
    public String[] listNames(String pathname) throws IOException {
        pathname = resolvePathname(pathname);
        try {
            if (pathname.isEmpty()) {
                pathname = ".";
            }
            if (!fileExists(pathname)) {
                return null;
            }
            if (!isDirectory(pathname)) {
                File remoteFile = new File(pathname);
                reply = "ls OK";
                return new String[] { remoteFile.getName() };
            }
            Vector<SFTPv3DirectoryEntry> files = getClient().ls(pathname);
            String[] rvFiles = new String[files.size()];
            for (int i = 0; i < files.size(); i++) {
                SFTPv3DirectoryEntry entry = files.get(i);
                rvFiles[i] = addFileSeparator(pathname) + entry.filename;
            }
            reply = "ls OK";
            return rvFiles;
        } catch (Exception e) {
            reply = e.toString();
            return null;
        }
    }

    public SOSFileList dir() {
        try {
            return dir(".");
        } catch (Exception e) {
            throw new RuntimeException(SOSVfs_E_130.params("dir"), e);
        }
    }

    public String getResponse() {
        return this.getReplyString();
    }

    public long size(String remoteFile) throws Exception {
        remoteFile = resolvePathname(remoteFile);
        long lngFileSize = -1;
        try {
            SFTPv3FileAttributes objAttr = getClient().stat(remoteFile);
            if (objAttr != null) {
                lngFileSize = objAttr.size.longValue();
            }
            return lngFileSize;
        } catch (com.trilead.ssh2.SFTPException e) {
            return lngFileSize;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfs_E_161.params("checking size", e));
        }
    }

    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) throws Exception {
        String sourceLocation = resolvePathname(remoteFile);
        SFTPv3FileHandle sftpFileHandle = null;
        FileOutputStream fos = null;
        File transferFile = null;
        long remoteFileSize = -1;
        try {
            transferFile = new File(localFile);
            remoteFileSize = size(remoteFile);
            sftpFileHandle = objFTPClient.openFileRO(sourceLocation);
            fos = null;
            long offset = 0;
            try {
                fos = new FileOutputStream(transferFile, append);
                byte[] buffer = new byte[32768];
                while (true) {
                    int len = objFTPClient.read(sftpFileHandle, offset, buffer, 0, buffer.length);
                    if (len <= 0) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                    offset += len;
                }
                fos.flush();
                fos.close();
                fos = null;
            } catch (Exception e) {
                throw new Exception(SOSVfs_E_161.params("get file [" + transferFile.getAbsolutePath() + "]", e.getMessage()));
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        fos = null;
                    } catch (Exception ex) {
                        // gracefully ignore this error
                    }
                }
            }
            objFTPClient.closeFile(sftpFileHandle);
            sftpFileHandle = null;
            if (remoteFileSize > 0 && remoteFileSize != transferFile.length()) {
                throw new Exception(SOSVfs_E_162.params(remoteFileSize, transferFile.length(), offset));
            }
            return transferFile.length();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                objFTPClient.closeFile(sftpFileHandle);
            } catch (Exception e) {
                //
            }
        }
    }

    public long readFile(final String pstrFilename) throws Exception {
        SFTPv3FileHandle sftpFileHandle = null;
        try {
            String sourceLocation = resolvePathname(pstrFilename);
            long remoteFileSize = size(sourceLocation);
            sftpFileHandle = openFileRO(pstrFilename);
            long offset = 0;
            try {
                byte[] buffer = new byte[32768];
                while (true) {
                    int len = objFTPClient.read(sftpFileHandle, offset, buffer, 0, buffer.length);
                    if (len <= 0) {
                        break;
                    }
                    offset += len;
                }
            } catch (Exception e) {
                throw new JobSchedulerException(SOSVfs_E_161.params("reading file [" + pstrFilename + "]", e.getMessage()));
            }
            return remoteFileSize;
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                objFTPClient.closeFile(sftpFileHandle);
                sftpFileHandle = null;
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public SFTPv3FileHandle openFileRO(final String pstrFilename) throws Exception {
        String sourceLocation = resolvePathname(pstrFilename);
        SFTPv3FileHandle sftpFileHandle = null;
        try {
            sftpFileHandle = objFTPClient.openFileRO(sourceLocation);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_161.params("opening file [" + sourceLocation + "]", e.getMessage()));
        }
        return sftpFileHandle;
    }

    public SFTPv3FileHandle openFileWR(final String pstrFilename) throws Exception {
        SFTPv3FileHandle sftpFileHandle = null;
        String sourceLocation = resolvePathname(pstrFilename);
        try {
            sftpFileHandle = objFTPClient.createFileTruncate(sourceLocation);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_161.params("opening file [" + sourceLocation + "]", e.getMessage()));
        }
        return sftpFileHandle;
    }

    private void closeObject(OutputStream objO) {
        try {
            if (objO != null) {
                objO.flush();
                objO.close();
                objO = null;
            }
        } catch (Exception e) {
            //
        }
    }

    private void closeInput(InputStream objO) {
        try {
            if (objO != null) {
                objO.close();
                objO = null;
            }
        } catch (IOException e) {
            //
        }
    }

    @Override
    public long getFile(final String remoteFile, final String localFile) {
        final boolean flgAppendLocalFile = false;
        long lngNoOfBytesRead = 0;
        try {
            lngNoOfBytesRead = this.getFile(remoteFile, localFile, flgAppendLocalFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return lngNoOfBytesRead;
    }

    @Override
    public void put(final String localFile, final String remoteFile) {
        try {
            long lngBytesWritten = this.putFile(localFile, remoteFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public long putFile(final String localFile, String remoteFile) throws Exception {
        long offset = 0;
        try {
            remoteFile = resolvePathname(remoteFile);
            SFTPv3FileHandle fileHandle = this.getClient().createFileTruncate(remoteFile);
            File localF = new File(localFile);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(localF);
                byte[] buffer = new byte[32768];
                while (true) {
                    int len = fis.read(buffer, 0, buffer.length);
                    if (len <= 0) {
                        break;
                    }
                    this.getClient().write(fileHandle, offset, buffer, 0, len);
                    offset += len;
                }
                fis.close();
                fis = null;
            } catch (Exception e) {
                raiseException(SOSVfs_E_161.params("writing file [" + localFile + "]", e.getMessage()));
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                        fis = null;
                    } catch (Exception ex) {
                        // gracefully ignore this error
                    }
                }
            }
            objFTPClient.closeFile(fileHandle);
            LOGGER.debug(SOSVfs_D_146.params(localFile, remoteFile));
            fileHandle = null;
            reply = "put OK";
            return offset;
        } catch (Exception e) {
            reply = e.toString();
            raiseException(e, SOSVfs_E_130.params("putFile()"));
        }
        return offset;
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
            return lngTotalBytesWritten;
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_130.params("putFile()"));
        } finally {
            closeInput(in);
            closeObject(out);
        }
        return lngTotalBytesWritten;
    }

    private void raiseException(final Exception e, final String pstrM) {
        LOGGER.error(pstrM + " (" + e.getMessage() + ")");
        throw new JobSchedulerException(pstrM, e);
    }

    private void raiseException(final String pstrM) {
        LOGGER.error(pstrM);
        throw new JobSchedulerException(pstrM);
    }

    @Override
    public long appendFile(final String localFile, final String remoteFile) {
        notImplemented();
        return -1;
    }

    @Override
    public void ascii() {
        //
    }

    @Override
    public void binary() {
        //
    }

    public int cd(final String directory) throws IOException {
        changeWorkingDirectory(directory);
        return 1;
    }

    private boolean fileExists(final String filename) {
        try {
            SFTPv3FileAttributes attributes = getClient().stat(filename);
            if (attributes != null) {
                return attributes.isRegularFile() || attributes.isDirectory();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean changeWorkingDirectory(String pathname) throws IOException {
        pathname = resolvePathname(pathname);
        if (pathname.length() > 1 && pathname.endsWith("/")) {
            pathname = pathname.substring(0, pathname.length() - 1);
        }
        if (!fileExists(pathname)) {
            reply = "\"" + pathname + "\" doesn't exist.";
            return false;
        }
        if (!isDirectory(pathname)) {
            reply = "\"" + pathname + "\" is not a directory.";
            return false;
        }
        if (pathname.startsWith("/") || currentDirectory.isEmpty()) {
            currentDirectory = pathname;
            reply = "cd OK";
            return true;
        }
        currentDirectory = pathname;
        reply = "cd OK";
        return true;
    }

    private String resolvePathname(final String pathname) {
        String strR = pathname;
        if (!pathname.startsWith("./") && !pathname.startsWith("/") && !currentDirectory.isEmpty()) {
            String slash = "";
            if (!currentDirectory.endsWith("/")) {
                slash = "/";
            }
            strR = currentDirectory + slash + pathname;
        }
        while (pathname.contains("\\")) {
            strR = pathname.replace('\\', '/');
        }
        if (strR.endsWith("/")) {
            strR = strR.substring(0, strR.length() - 1);
        }
        return strR;
    }

    @Override
    public void delete(final String pathname) throws IOException {
        getClient().rm(pathname);
        reply = "rm OK";
        LOGGER.info(SOSVfs_I_131.params(pathname, getReplyString()));
    }

    @Override
    public void login(final String strUserName, final String strPassword) {
        boolean isAuthenticated = false;
        try {
            gstrUser = strUserName;
            LOGGER.debug(SOSVfs_D_132.params(strUserName));
            isAuthenticated = sshConnection.authenticateWithPassword(strUserName, strPassword);
            if (!isAuthenticated) {
                raiseException(SOSVfs_E_134.params("authentication"));
            }
            reply = "OK";
            LOGGER.info(SOSVfs_D_133.params(strUserName));
            logReply();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        logReply();
    }

    @Override
    public void disconnect() {
        reply = "disconnect OK";
        if (objFTPClient != null) {
            try {
                objFTPClient.close();
                objFTPClient = null;
            } catch (Exception ex) {
                reply = "disconnect: " + ex;
            }
        }
        if (sshConnection != null) {
            try {
                sshConnection.close();
                sshConnection = null;
            } catch (Exception ex) {
                reply = "disconnect: " + ex;
            }
        }
        isConnected = false;
        LOGGER.info(reply);
    }

    @Override
    public String getReplyString() {
        return reply;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void logout() {
        try {
            if (isConnected()) {
                disconnect();
                LOGGER.debug(SOSVfs_D_138.params(objHost.getValue(), getReplyString()));
            } else {
                LOGGER.info(SOSVfs_I_139.get());
            }
        } catch (Exception e) {
            LOGGER.warn(SOSVfs_W_140.get() + e.getMessage());
        }
    }

    @Override
    public void rename(String from, String to) {
        from = resolvePathname(from);
        to = resolvePathname(to);
        try {
            getClient().mv(from, to);
        } catch (Exception e) {
            reply = e.toString();
            raiseException(e, SOSVfs_E_134.params("rename()"));
        }
        reply = "mv OK";
        LOGGER.info(SOSVfs_I_150.params(from, to));
    }

    @Override
    public ISOSVFSHandler getHandler() {
        return this;
    }
    @Override
    public void executeCommand(final String strCmd) throws Exception {
        executeCommand(strCmd, null);
    }
    
    @Override
    public void executeCommand(final String strCmd, SOSVfsEnv env) throws Exception {
        final String strEndOfLine = System.getProperty("line.separator");
        if (sshSession == null) {
            sshSession = sshConnection.openSession();
        }
        sshSession.execCommand(strCmd);
        LOGGER.debug(SOSVfs_D_163.params("stdout", strCmd));
        InputStream ipsStdOut = new StreamGobbler(sshSession.getStdout());
        InputStream ipsStdErr = new StreamGobbler(sshSession.getStderr());
        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(ipsStdOut));
        StringBuffer strbStdoutOutput = new StringBuffer();
        while (true) {
            String line = stdoutReader.readLine();
            if (line == null) {
                break;
            }
            strbStdoutOutput.append(line + strEndOfLine);
        }
        LOGGER.debug(strbStdoutOutput);
        LOGGER.debug(SOSVfs_D_163.params("stderr", strCmd));
        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(ipsStdErr));
        StringBuilder strbStderrOutput = new StringBuilder();
        while (true) {
            String line = stderrReader.readLine();
            if (line == null) {
                break;
            }
            strbStderrOutput.append(line + strEndOfLine);
        }
        LOGGER.debug(strbStderrOutput);
        int res = sshSession.waitForCondition(ChannelCondition.EOF, 30 * 1000);
        Integer intExitCode = sshSession.getExitStatus();
        if (intExitCode != null && !intExitCode.equals(new Integer(0))) {
            throw new JobSchedulerException(SOSVfs_E_164.params(intExitCode));
        }
        sshSession.close();
        sshSession = null;
    }

    @Override
    public String createScriptFile(final String pstrContent) throws Exception {
        notImplemented();
        return null;
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
    public ISOSConnection authenticate(final ISOSAuthenticationOptions pobjAO) throws Exception {
        objAO = pobjAO;
        doAuthenticate(pobjAO);
        return this;
    }

    private ISOSConnection doAuthenticate(final ISOSAuthenticationOptions pobjAO) throws Exception {
        boolean isAuthenticated = false;
        try {
            gstrUser = pobjAO.getUser().getValue();
            String strPW = pobjAO.getPassword().getValue();
            String strUserName = gstrUser;
            LOGGER.debug(SOSVfs_D_132.params(strUserName));
            if (pobjAO.getAuthMethod().isPublicKey()) {
                LOGGER.debug(SOSVfs_D_165.params("userid", "publickey"));
                SOSOptionInFileName objAF = pobjAO.getAuthFile();
                objAF.checkMandatory(true);
                if (objAF.isNotEmpty()) {
                    char[] chrAFContent = objAF.getJSFile().file2String().toCharArray();
                    isAuthenticated = sshConnection.authenticateWithPublicKey(strUserName, chrAFContent, strPW);
                }
            } else {
                if (pobjAO.getAuthMethod().isPassword()) {
                    LOGGER.debug(SOSVfs_D_165.params("userid", "password"));
                    isAuthenticated = sshConnection.authenticateWithPassword(gstrUser, strPW);
                } else {
                    throw new Exception(SOSVfs_E_166.params(pobjAO.getAuthMethod().getValue()));
                }
            }
            if (!isAuthenticated) {
                throw new JobSchedulerException(SOSVfs_E_167.params(pobjAO.getAuthMethod().getValue(), pobjAO.getAuthFile().getValue()));
            }
            reply = "OK";
            LOGGER.info(SOSVfs_D_133.params(strUserName));
            logReply();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_168.get(), e);
        }
        return this;
    }

    @Override
    public void closeConnection() throws Exception {
        if (isConnected()) {
            disconnect();
            LOGGER.debug(SOSVfs_D_125.params(objConnectionOptions.getHost().getValue()));
            logReply();
        }
    }

    private SFTPv3Client getClient() {
        if (objFTPClient == null) {
            try {
                objFTPClient = new SFTPv3Client(sshConnection);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return objFTPClient;
    }

    @Override
    public ISOSConnection connect() {
        LOGGER.debug(SOSVfs_D_169.get());
        try {
            this.doConnect(objConnectionOptions.getHost().getValue(), objConnectionOptions.getPort().value());
            LOGGER.info(SOSVfs_D_0102.params(objConnectionOptions.getHost().getValue(), objConnectionOptions.getPort().value()));
        } catch (RuntimeException e) {
            if (objConnectionOptions.getAlternativeHost().isNotEmpty() && objConnectionOptions.getAlternativePort().isNotEmpty()) {
                LOGGER.info(SOSVfs_E_204.get());
                this.doConnect(objConnectionOptions.getAlternativeHost().getValue(), objConnectionOptions.getAlternativePort().value());
                LOGGER.info(SOSVfs_D_0102.params(objConnectionOptions.getAlternativeHost().getValue(),
                        objConnectionOptions.getAlternativePort().value()));
            } else {
                throw e;
            }
        }
        isConnected = true;
        reply = "OK";
        return this;
    }

    @Override
    public ISOSConnection connect(final SOSConnection2OptionsAlternate pobjConnectionOptions) {
        objConnection2Options = pobjConnectionOptions;
        try {
            objHost = objConnection2Options.getHost();
            objHost.checkMandatory();
            objPort = objConnection2Options.getPort();
            objPort.checkMandatory();
            host = objHost.getValue();
            port = objPort.value();
            this.connect(objHost.getValue(), objPort.value());
            if (!isConnected) {
                SOSConnection2OptionsSuperClass objAlternate = objConnection2Options.getAlternatives();
                objHost = objAlternate.host;
                objPort = objAlternate.port;
                host = objHost.getValue();
                port = objPort.value();
                LOGGER.info(SOSVfs_I_170.params(host));
                this.connect(objHost.getValue(), objPort.value());
                if (!isConnected) {
                    objHost = null;
                    objPort = null;
                    host = "";
                    port = -1;
                    raiseException(SOSVfs_E_204.get());
                }
            }
        } catch (Exception e) {
            LOGGER.error("exception occured", e);
            throw new JobSchedulerException("exception occured:", e);
        }
        return this;
    }

    @Override
    public void getOptions(final SOSFTPOptions pobjOptions) {
        super.getOptions(pobjOptions);
        if (pobjOptions.bufferSize.isDirty()) {
            int intBufferSize = pobjOptions.bufferSize.value();
            Channel.CHANNEL_BUFFER_SIZE = intBufferSize;
            SFTPv3Client.MAX_RECEIVE_BUFFER_SIZE = intBufferSize;
        }
    }

    @Override
    public ISOSConnection connect(final ISOSConnectionOptions pobjConnectionOptions) throws Exception {
        objConnectionOptions = pobjConnectionOptions;
        try {
            String host = objConnectionOptions.getHost().getValue();
            int port = objConnectionOptions.getPort().value();
            this.connect(host, port);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return this;
    }

    @Override
    public ISOSConnection connect(final String pstrHostName, final int pintPortNumber) throws Exception {
        this.connect(pstrHostName, pintPortNumber);
        if (objConnectionOptions != null) {
            objConnectionOptions.getHost().setValue(pstrHostName);
            objConnectionOptions.getPort().value(pintPortNumber);
        }
        return this;
    }

    @Override
    public void closeSession() throws Exception {
        this.logout();
    }

    @Override
    public ISOSSession openSession(final ISOSShellOptions pobjShellOptions) throws Exception {
        notImplemented();
        return null;
    }

    @Override
    public ISOSVirtualFile transferMode(final SOSOptionTransferMode pobjFileTransferMode) {
        if (pobjFileTransferMode.isAscii()) {
            this.ascii();
            LOGGER.debug(SOSVfs_D_122.params("ASCII"));
            LOGGER.debug(SOSVfs_D_123.params("ascii", getReplyString()));
        } else {
            this.binary();
            LOGGER.debug(SOSVfs_D_122.params("binary"));
            LOGGER.debug(SOSVfs_D_123.params("binary", getReplyString()));
        }
        return null;
    }

    public SOSFileListEntry getNewVirtualFile(final String pstrFileName) {
        SOSFileListEntry objF = new SOSFileListEntry(pstrFileName);
        objF.setVfsHandler(this);
        return objF;
    }

    @Override
    public ISOSVirtualFolder mkdir(final SOSFolderName pobjFolderName) {
        this.mkdir(pobjFolderName.getValue());
        return null;
    }

    @Override
    public boolean rmdir(final SOSFolderName pobjFolderName) throws IOException {
        this.rmdir(pobjFolderName.getValue());
        return true;
    }

    @Override
    public ISOSConnection getConnection() {
        return this;
    }

    @Override
    public ISOSSession getSession() {
        return null;
    }

    @Override
    public SOSFileList dir(final SOSFolderName pobjFolderName) {
        this.dir(pobjFolderName.getValue());
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

    @Override
    public boolean remoteIsWindowsShell() {
        return false;
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        //
    }

    @Override
    public ISOSVirtualFile getFileHandle(String pstrFilename) {
        pstrFilename = pstrFilename.replaceAll("\\\\", "/");
        ISOSVirtualFile objFtpFile = new SOSVfsSFtpFile(pstrFilename);
        objFtpFile.setHandler(this);
        return objFtpFile;
    }

    @Override
    public String[] getFilelist(final String folder, final String regexp, final int flag, final boolean withSubFolder, String integrityHashType) {
        vecDirectoryListing = null;
        if (vecDirectoryListing == null) {
            String strT = folder.replaceAll("\\\\", "/");
            vecDirectoryListing = nList(folder, withSubFolder);
        }
        Vector<String> strB = new Vector<String>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (String strFile : vecDirectoryListing) {
            String strFileName = new File(strFile).getName();
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

    @Override
    public String[] getFolderlist(final String folder, final String regexp, final int flag, final boolean withSubFolder) {
        vecDirectoryListing = null;
        if (vecDirectoryListing == null) {
            String strT = folder.replaceAll("\\\\", "/");
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

    @Override
    public OutputStream getAppendFileStream(final String strFileName) {
        return null;
    }

    @Override
    public long getFileSize(final String strFileName) {
        final String conMethodName = "SOSVfsSFtp::getFileSize";
        long lngFileSize = 0;
        try {
            String lstrFileName = strFileName.replaceAll("\\\\", "/");
            lngFileSize = this.size(lstrFileName);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_153.params(conMethodName));
        }
        return lngFileSize;
    }

    @Override
    public InputStream getInputStream(final String strFileName) {
        return null;
    }

    @Override
    public String getModificationTime(final String strFileName) {
        return "";
    }

    @Override
    public OutputStream getOutputStream(final String strFileName) {
        return null;
    }

    @Override
    public void close() {
        //
    }

    @Override
    public void closeInput() {
        //
    }

    @Override
    public void closeOutput() {
        //
    }

    @Override
    public void flush() {
        //
    }

    @Override
    public int read(final byte[] bteBuffer) {
        int intL = -1;
        try {
            int intMaxBuffLen = bteBuffer.length;
            intL = objFTPClient.read(objInputFile, lngReadOffset, bteBuffer, 0, intMaxBuffLen);
            lngReadOffset += intL;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return intL;
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        int intL = -1;
        try {
            intL = objFTPClient.read(objInputFile, lngReadOffset, bteBuffer, 0, intLength);
            lngReadOffset += intL;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return intL;
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        try {
            objFTPClient.write(objOutputFile, lngWriteOffset, bteBuffer, 0, intLength);
            lngWriteOffset += intLength;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void write(final byte[] bteBuffer) {
        //
    }

    @Override
    public void openInputFile(final String pstrFileName) {
        try {
            if (objInputFile == null || objInputFile.isClosed()) {
                objInputFile = openFileRO(pstrFileName);
                lngReadOffset = 0;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void openOutputFile(final String pstrFileName) {
        try {
            objOutputFile = openFileWR(pstrFileName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles(final String string) {
        return null;
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles() {
        return null;
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
            throw new JobSchedulerException(SOSVfs_E_130.params("putFile()"), e);
        }
    }

    @Override
    public void controlEncoding(final String pstrControlEncoding) {
        //
    }

    public SFTPv3FileHandle getOutputFileHandle(final String pstrFileName) {
        openOutputFile(pstrFileName);
        return objOutputFile;
    }

    public SFTPv3FileHandle getInputFileHandle(final String pstrFileName) {
        openInputFile(pstrFileName);
        return objInputFile;
    }

    @Override
    public void doPostLoginOperations() {
        //
    }

    @Override
    public ISOSConnection connect(final ISOSDataProviderOptions pobjConnectionOptions) throws Exception {
        return null;
    }

    @Override
    public OutputStream getFileOutputStream() {
        return null;
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

}