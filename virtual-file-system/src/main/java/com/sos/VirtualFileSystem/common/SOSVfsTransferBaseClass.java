package com.sos.VirtualFileSystem.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
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
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSVfsTransferBaseClass extends SOSVfsBaseClass implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem,
        ISOSConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsTransferBaseClass.class);
    private List<SOSFileEntry> directoryListing = null;

    protected SOSConnection2OptionsAlternate connection2OptionsAlternate = null;
    protected ISOSAuthenticationOptions authenticationOptions = null;

    protected String authenticationFilename = "";
    protected String host = EMPTY_STRING;
    protected int port = 0;
    protected String userName = EMPTY_STRING;
    protected String reply = "OK";
    protected String currentDirectory = "";

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
            LOGGER.debug(SOSVfs_D_141.params("pwd."));
            return this.getCurrentPath();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("pwd"), e);
        }
    }

    protected String getCurrentPath() {
        return null;
    }

    protected boolean logReply() {
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
    public List<SOSFileEntry> nList(final String pathname, final boolean flgRecurseSubFolder, boolean checkIfExists) {
        try {
            return getFilenames(pathname, flgRecurseSubFolder, 0, checkIfExists);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("nList"), e);
        }
    }

    @Override
    public boolean isDirectory(final String filename) {
        LOGGER.info("not implemented yet");
        return false;
    }

    @Override
    public List<SOSFileEntry> listNames(final String path, boolean checkIfExists, boolean checkIfIsDirector) throws IOException {
        LOGGER.info("not implemented yet");
        return null;
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
            //
        }
    }

    protected void closeInput(InputStream objO) {
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
        LOGGER.info("not implemented yet");
        return 0;
    }

    public long putFile(final String localFile, final OutputStream out) {
        if (out == null) {
            throw new JobSchedulerException(SOSVfs_E_147.get());
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
            throw new JobSchedulerException(SOSVfs_E_130.params("putFile()"), e);
        } finally {
            closeInput(in);
            closeObject(out);
        }
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
        LOGGER.info("not implemented yet");
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
                LOGGER.debug(SOSVfs_D_138.params(host, getReplyString()));
            } else {
                LOGGER.info("not connected, logout useless.");
            }
        } catch (Exception e) {
            LOGGER.warn(SOSVfs_W_140.get() + e.getMessage(), e);
        }
    }

    @Override
    public ISOSVFSHandler getHandler() {
        return this;
    }

    @Override
    public void executeCommand(final String strCmd) throws Exception {
    }

    @Override
    public void executeCommand(final String strCmd, SOSVfsEnv env) throws Exception {
        LOGGER.info("not implemented yet");
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
            LOGGER.debug(SOSVfs_D_125.params(host));
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
        return new SOSFileListEntry(pstrFileName);
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
    public List<SOSFileEntry> dir(final SOSFolderName pobjFolderName) {
        try {
            return getFilenames(pobjFolderName.getValue(), false, 0, true);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }//
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
    public List<SOSFileEntry> getFilelist(final String folder, final String regexp, final int flag, final boolean withSubFolder,
            boolean checkIfExists, String integrityHashType) {
        List<SOSFileEntry> result = nList(folder, withSubFolder, checkIfExists);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][total] %s files", folder, result.size()));
        }

        List<SOSFileEntry> list = new ArrayList<SOSFileEntry>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (SOSFileEntry fe : result) {
            Matcher matcher = pattern.matcher(fe.getFilename());
            if (matcher.find()) {
                list.add(fe);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][filtered] %s files", folder, list.size()));
        }
        return list;
    }

    @Override
    public List<SOSFileEntry> getFolderlist(final String folder, final String regexp, final int flag, final boolean withSubFolder) {
        List<SOSFileEntry> result = nList(folder, withSubFolder, true);

        List<SOSFileEntry> list = new ArrayList<SOSFileEntry>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (SOSFileEntry entry : result) {
            Matcher matcher = pattern.matcher(entry.getFilename());
            if (matcher.find()) {
                list.add(entry);
            }
        }
        return list;
    }

    @Override
    public OutputStream getAppendFileStream(final String strFileName) {
        OutputStream objO = null;
        return objO;
    }

    protected long size(final String strFileName) throws Exception {
        LOGGER.info("not implemented yet");
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
        LOGGER.info("not implemented yet");
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
        LOGGER.info("not implemented yet");
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
        }
    }

    @Override
    public void mkdir(final String pathname) throws IOException {
        LOGGER.info("not implemented yet");
    }

    @Override
    public void rmdir(final String pstrFolderName) throws IOException {
        LOGGER.info("not implemented yet");
    }

    @Override
    public void delete(final String pathname, boolean checkIsDirectory) throws IOException {
        LOGGER.info("not implemented yet");
    }

    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) throws Exception {
        return 0;
    }

    @Override
    public void rename(final String from, final String to) {
        LOGGER.info("not implemented yet");
    }

    private List<SOSFileEntry> getFilenames(String path, final boolean recurseSubFolders, int recLevel, boolean checkIfExists) throws Exception {
        if (recLevel == 0) {
            directoryListing = new ArrayList<SOSFileEntry>();
            this.doPWD();
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
            /** if (currentFile.indexOf("/") == -1) { currentFile = path + "/" + currentFile; currentFile = currentFile.replaceAll("//+", "/"); } if
             * (this.isDirectory(currentFile)) { if (recurseSubFolders) { recLevel++; this.getFilenames(currentFile, recurseSubFolders, recLevel); } } else {
             * directoryListing.add(currentFile); } */
            if (entry.isDirectory()) {
                if (recurseSubFolders) {
                    recLevel++;
                    getFilenames(entry.getFullPath(), recurseSubFolders, recLevel, checkIfExists);
                }
            } else {
                directoryListing.add(entry);
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

    @Override
    public List<SOSFileEntry> dir(String pathname, int flag) {
        notImplemented();
        return null;
    }

}