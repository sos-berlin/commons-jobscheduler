package com.sos.VirtualFileSystem.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
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
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSVfsTransferBaseClass extends SOSVfsBaseClass implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem,
        ISOSConnection {

    protected String authenticationFilename = "";
    protected String host = EMPTY_STRING;
    protected int port = 0;
    protected String userName = EMPTY_STRING;
    protected String reply = "OK";
    protected String currentDirectory = "";
    protected SOSConnection2OptionsAlternate connection2OptionsAlternate = null;
    protected ISOSAuthenticationOptions authenticationOptions = null;
    private static final Logger LOGGER = Logger.getLogger(SOSVfsTransferBaseClass.class);
    private Vector<String> directoryListing = null;

    public SOSVfsTransferBaseClass() {
        super();
    }

    protected String getHostID(final String msg) {
        return "(" + userName + "@" + host + ":" + port + ") " + msg;
    }

    @Override
    public int passive() {
        return 0;
    }

    @Override
    public String doPWD() {
        try {
            logDEBUG(SOSVfs_D_141.params("pwd."));
            return this.getCurrentPath();
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_134.params("pwd"));
            return null;
        }
    }

    protected String getCurrentPath() {
        return null;
    }

    protected boolean logReply() {
        reply = getReplyString();
        if (!reply.trim().isEmpty()) {
            logDEBUG(reply);
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

    public void setStrictHostKeyChecking(final String strictHostKeyCheckingValue) {

    }

    private boolean isPositiveCommandCompletion() {
        int x = 0;
        return x <= 300;
    }

    public boolean isNotHiddenFile(final String fileName) {
        if (fileName == null || ".".equals(fileName) || "..".equals(fileName) || fileName.endsWith("/..") || fileName.endsWith("/.")) {
            return false;
        }
        return true;
    }

    @Override
    public Vector<String> nList(final String pathname) {
        return getFilenames(pathname);
    }

    @Override
    public Vector<String> nList(final String pathname, final boolean flgRecurseSubFolder) {
        Vector<String> result = null;
        try {
            result = getFilenames(pathname, flgRecurseSubFolder);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_134.params("nList"));
        }
        return result;
    }

    @Override
    public Vector<String> nList() {
        Vector<String> result = null;
        try {
            result = getFilenames();
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_134.params("nList"));
        }
        return result;
    }

    @Override
    public Vector<String> nList(final boolean recursive) {
        Vector<String> result = null;
        try {
            result = getFilenames(recursive);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_134.params("nList"));
        }
        return result;
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
            raiseException(e, SOSVfs_E_128.params(listFiles, "dir()"));
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
        logINFO("not implemented yet");
        return false;
    }

    @Override
    public String[] listNames(final String path) throws IOException {
        logINFO("not implemented yet");
        return null;
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

    protected String trimResponseCode(final String response) throws Exception {
        if (response.length() < 5) {
            return response;
        }
        return response.substring(4).trim();
    }

    protected void closeObject(OutputStream objO) {
        try {
            if (objO != null) {
                objO.flush();
                objO.close();
                objO = null;
            }
        } catch (Exception e) {
        }
    }

    protected void closeInput(InputStream objO) {
        try {
            if (objO != null) {
                objO.close();
                objO = null;
            }
        } catch (IOException e) {
        }

    }

    @Override
    public long getFile(final String remoteFile, final String localFile) {
        final boolean flgAppendLocalFile = false;
        long lngNoOfBytesRead = 0;
        try {
            lngNoOfBytesRead = this.getFile(remoteFile, localFile, flgAppendLocalFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return lngNoOfBytesRead;
    }

    @Override
    public void put(final String localFile, final String remoteFile) {

        this.putFile(localFile, remoteFile);
    }

    @Override
    public long putFile(final String localFile, final String remoteFile) {
        logINFO("not implemented yet");
        return 0;
    }

    public long putFile(final String localFile, final OutputStream out) {
        if (out == null) {
            raiseException(SOSVfs_E_147.get());
        }
        FileInputStream in = null;
        long bytesWrittenTotal = 0;
        try {
            byte[] buffer = new byte[4096];
            in = new FileInputStream(new File(localFile));
            int bytesWritten;
            synchronized (this) {
                while ((bytesWritten = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesWritten);
                    bytesWrittenTotal += bytesWritten;
                }
            }
            closeInput(in);
            closeObject(out);
            return bytesWrittenTotal;
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_130.params("putFile()"));
        } finally {
            closeInput(in);
            closeObject(out);
        }
        return bytesWrittenTotal;
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

    public int cd(final String directory) throws Exception {
        changeWorkingDirectory(directory);
        return 1;
    }

    protected boolean fileExists(final String filename) {
        return false;
    }

    @Override
    public boolean changeWorkingDirectory(final String pathname) throws IOException {
        logINFO("not implemented yet");
        return true;
    }

    protected String resolvePathname(String pathname) {
        pathname = pathname.replaceAll("\\\\", "/");
        return pathname;
    }

    @Override
    public void login(final String strUserName, final String strPassword) {

    }

    @Override
    public void disconnect() {
    }

    @Override
    public String getReplyString() {
        return reply;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void logout() {
        try {
            if (isConnected()) {
                disconnect();
                logDEBUG(SOSVfs_D_138.params(host, getReplyString()));
            } else {
                logINFO("not connected, logout useless.");
            }
        } catch (Exception e) {
            logWARN(SOSVfs_W_140.get() + e.getMessage());
        }
    }

    @Override
    public ISOSVFSHandler getHandler() {
        return this;
    }

    @Override
    public void executeCommand(final String strCmd) throws Exception {
        logINFO("not implemented yet");
    }

    @Override
    public void executeCommand(final String strCmd, SOSVfsEnv env) throws Exception {
        logINFO("not implemented yet");
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
        return this;
    }

    @Override
    public void closeConnection() throws Exception {
        if (isConnected()) {
            disconnect();
            logDEBUG(SOSVfs_D_125.params(host));
            logReply();
        }
    }

    @Override
    public ISOSConnection connect() throws Exception {
        notImplemented();
        return this;
    }

    @Override
    public ISOSConnection connect(final SOSConnection2OptionsAlternate pobjConnectionOptions) throws Exception {
        return this;
    }

    @Override
    public ISOSConnection connect(final ISOSConnectionOptions pobjConnectionOptions) throws Exception {
        notImplemented();
        return this;
    }

    @Override
    public ISOSConnection connect(final String pstrHostName, final int pintPortNumber) throws Exception {
        notImplemented();
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
        } else {
            this.binary();
        }
        return null;
    }

    public SOSFileListEntry getNewVirtualFile(final String pstrFileName) {
        SOSFileListEntry objF = new SOSFileListEntry(pstrFileName);
        objF.setVfsHandler(this);
        return objF;
    }

    @Override
    public ISOSVirtualFolder mkdir(final SOSFolderName pobjFolderName) throws IOException {
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

    }

    @Override
    public ISOSVirtualFile getFileHandle(final String pstrFilename) {
        return null;
    }

    @Override
    public String[] getFilelist(final String folder, final String regexp, final int flag, final boolean withSubFolder, String integrityHashType) {
        Vector<String> result = nList(folder, withSubFolder);
        Vector<String> newResult = new Vector<String>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (String strFile : result) {
            Matcher matcher = pattern.matcher(new File(strFile).getName());
            if (matcher.find()) {
                newResult.add(strFile);
            }
        }
        return newResult.toArray(new String[newResult.size()]);
    }

    @Override
    public String[] getFolderlist(final String folder, final String regexp, final int flag, final boolean withSubFolder) {
        Vector<String> result = nList(folder, withSubFolder);
        Vector<String> newResult = new Vector<String>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (String strFile : result) {
            Matcher matcher = pattern.matcher(new File(strFile).getName());
            if (matcher.find()) {
                newResult.add(strFile);
            }
        }
        return newResult.toArray(new String[newResult.size()]);
    }

    @Override
    public OutputStream getAppendFileStream(final String strFileName) {
        OutputStream objO = null;
        return objO;
    }

    protected long size(final String strFileName) throws Exception {
        logINFO("not implemented yet");
        return 0;
    }

    @Override
    public long getFileSize(final String strFileName) {
        long lngFileSize = 0;
        try {
            String lstrFileName = strFileName.replaceAll("\\\\", "/");
            lngFileSize = this.size(lstrFileName);
        } catch (Exception e) {
            LOGGER.trace(SOSVfs_E_134.params("getFileSize()") + ":" + e.getMessage());
        }
        return lngFileSize;
    }

    @Override
    public InputStream getInputStream(final String strFileName) {
        InputStream objI = null;
        return objI;
    }

    @Override
    public String getModificationTime(final String fileName) {
        logINFO("not implemented yet");
        return null;
    }

    @Override
    public abstract OutputStream getOutputStream(final String fileName);

    @Override
    public void close() {
    }

    @Override
    public void closeInput() {
    }

    @Override
    public void closeOutput() {
    }

    @Override
    public void flush() {
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
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        logINFO("not implemented yet");
    }

    @Override
    public void write(final byte[] bteBuffer) {

    }

    @Override
    public void openInputFile(final String pstrFileName) {

    }

    @Override
    public void openOutputFile(final String pstrFileName) {
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
            throw new JobSchedulerException(SOSVfs_E_130.params("putfile()"), e);
        } finally {
        }
    }

    @Override
    public void controlEncoding(final String pstrControlEncoding) {

    }

    @Override
    public void mkdir(final String pathname) throws IOException {
        logINFO("not implemented yet");
    }

    @Override
    public void rmdir(final String pstrFolderName) throws IOException {
        logINFO("not implemented yet");
    }

    @Override
    public void delete(final String pathname) throws IOException {
        logINFO("not implemented yet");
    }

    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) throws Exception {
        return 0;
    }

    @Override
    public void rename(final String from, final String to) {
        logINFO("not implemented yet");
    }

    protected void raiseException(final Exception e, final String msg) {
        LOGGER.error(msg + " (" + e.getMessage() + ")");
        throw new JobSchedulerException(msg, e);
    }

    protected void raiseException(final String msg) {
        logEXCEPTION(msg);
        throw new JobSchedulerException(msg);
    }

    private String getLogPrefix() {
        return getLogPrefix(4);
    }

    private String getLogPrefix(final int level) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[level];
        String[] classNameArr = ste.getClassName().split("\\.");
        return "(" + classNameArr[classNameArr.length - 1] + "::" + ste.getMethodName() + ") ";
    }

    protected void logINFO(final Object msg) {
        LOGGER.info(this.getLogPrefix() + msg);
    }

    protected void logDEBUG(final Object msg) {
        LOGGER.debug(this.getLogPrefix() + msg);
    }

    protected void logWARN(final Object msg) {
        LOGGER.warn(this.getLogPrefix() + msg);
    }

    protected void logERROR(final Object msg) {
        LOGGER.error(this.getLogPrefix() + msg);
    }

    protected void logEXCEPTION(final Object msg) {
        LOGGER.error(this.getLogPrefix(4) + msg);
    }

    private Vector<String> getFilenames() throws Exception {
        return getFilenames("", false);
    }

    private Vector<String> getFilenames(final boolean flgRecurseSubFolders) throws Exception {
        return getFilenames("", flgRecurseSubFolders);
    }

    private Vector<String> getFilenames(final String pathname) {
        try {
            return getFilenames(pathname, false);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_130.params("getFilelist"));
            return null;
        }
    }

    private Vector<String> getFilenames(final String path, final boolean recurseSubFolders) throws Exception {
        return getFilenames(path, recurseSubFolders, 0);
    }

    private Vector<String> getFilenames(String path, final boolean recurseSubFolders, int recLevel) throws Exception {
        String currentPath = "";
        if (recLevel == 0) {
            directoryListing = new Vector<String>();
            currentPath = this.doPWD();
        }
        String[] fileList = null;
        path = path.trim();
        if (path.isEmpty()) {
            path = ".";
        }
        try {
            fileList = listNames(path);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        if (fileList == null) {
            return directoryListing;
        }
        for (String currentFile : fileList) {
            if (!isNotHiddenFile(currentFile)) {
                continue;
            }
            if (currentFile.indexOf("/") == -1) {
                currentFile = path + "/" + currentFile;
                currentFile = currentFile.replaceAll("//+", "/");
            }
            if (this.isDirectory(currentFile)) {
                if (recurseSubFolders) {
                    recLevel++;
                    this.getFilenames(currentFile, recurseSubFolders, recLevel);
                }
            } else {
                directoryListing.add(currentFile);
            }
        }
        return directoryListing;
    }

    @Override
    public void doPostLoginOperations() {

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

}