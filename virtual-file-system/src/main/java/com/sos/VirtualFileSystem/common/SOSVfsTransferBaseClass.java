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

    @Override
    public void logout() {
        try {
            if (isConnected()) {
                disconnect();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(SOSVfs_D_138.params(host, getReplyString()));
                }
            } else {
                LOGGER.info("not connected, logout useless.");
            }
        } catch (Exception e) {
            LOGGER.warn(SOSVfs_W_140.get() + e.getMessage(), e);
        }
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
    public void closeConnection() throws Exception {
        if (isConnected()) {
            disconnect();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(SOSVfs_D_125.params(host));
            }
            logReply();
        }
    }

    @Override
    public List<SOSFileEntry> dir(final SOSFolderName folderName) {
        try {
            return getFilenames(folderName.getValue(), false, 0, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }//
    }

    @Override
    public List<SOSFileEntry> nList(final String pathname, final boolean recursive, boolean checkIfExists) {
        try {
            return getFilenames(pathname, recursive, 0, checkIfExists);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("nList"), e);
        }
    }

    private List<SOSFileEntry> getFilenames(String path, final boolean recursive, int recLevel, boolean checkIfExists) throws Exception {
        if (recLevel == 0) {
            directoryListing = new ArrayList<SOSFileEntry>();
            doPWD();
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
            String integrityHashType) {
        List<SOSFileEntry> result = nList(folder, recursive, checkIfExists);
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
    public List<SOSFileEntry> getFolderlist(final String folder, final String regexp, final int flag, final boolean recursive) {
        List<SOSFileEntry> result = nList(folder, recursive, true);

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
    public long getFile(final String remoteFile, final String localFile) {
        try {
            return getFile(remoteFile, localFile, false);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        return 0;
    }

    @Override
    public void put(final String localFile, final String remoteFile) {
        putFile(localFile, remoteFile);
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

    public int cd(final String directory) throws Exception {
        changeWorkingDirectory(directory);
        return 1;
    }

    public static String normalizePath(String path) {
        return path.replaceAll("\\\\", "/");
    }

    @Override
    public void closeSession() throws Exception {
        logout();
    }

    @Override
    public ISOSVirtualFile transferMode(final SOSOptionTransferMode mode) {
        if (mode.isAscii()) {
            ascii();
        } else {
            binary();
        }
        return null;
    }

    public SOSFileListEntry getNewVirtualFile(final String fileName) {
        return new SOSFileListEntry(fileName);
    }

    @Override
    public ISOSVirtualFolder mkdir(final SOSFolderName folderName) throws IOException {
        mkdir(folderName.getValue());
        return null;
    }

    @Override
    public boolean rmdir(final SOSFolderName folderName) throws IOException {
        rmdir(folderName.getValue());
        return true;
    }

    @Override
    public long getFileSize(final String fileName) {
        try {
            return size(normalizePath(fileName));
        } catch (Exception e) {
            LOGGER.trace(SOSVfs_E_134.params("getFileSize") + ":" + e.toString(), e);
        }
        return 0;
    }

    @Override
    public void putFile(final ISOSVirtualFile file) {
        OutputStream os = null;
        InputStream is = null;
        try {
            os = getFileHandle(file.getName()).getFileOutputStream();
            is = file.getFileInputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
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
            throw new JobSchedulerException(SOSVfs_E_130.params("putfile()"), e);
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

    protected String getHostID(final String msg) {
        return "(" + userName + "@" + host + ":" + port + ") " + msg;
    }

    @Override
    public String doPWD() {
        try {
            LOGGER.debug(SOSVfs_D_141.params("doPWD"));
            return this.getCurrentPath();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("pwd"), e);
        }
    }

    protected boolean logReply() {
        reply = getReplyString();
        if (!reply.trim().isEmpty()) {
            LOGGER.debug(reply);
        }
        return true;
    }

    public boolean isNotHiddenFile(final String fileName) {
        if (fileName == null || ".".equals(fileName) || "..".equals(fileName) || fileName.endsWith("/..") || fileName.endsWith("/.")) {
            return false;
        }
        return true;
    }

    protected String trimResponseCode(final String response) throws Exception {
        if (response.length() < 5) {
            return response;
        }
        return response.substring(4).trim();
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

    @Override
    public String getReplyString() {
        return reply;
    }

    @Override
    public ISOSVFSHandler getHandler() {
        return this;
    }

    @Override
    public ISOSConnection authenticate(final ISOSAuthenticationOptions options) throws Exception {
        return this;
    }

    @Override
    public ISOSConnection connect(final SOSConnection2OptionsAlternate options) throws Exception {
        return this;
    }

    @Override
    public ISOSConnection connect(final ISOSDataProviderOptions options) throws Exception {
        return null;
    }

    @Override
    public ISOSConnection getConnection() {
        return this;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public ISOSSession getSession() {
        return null;
    }

    @Override
    public void login(final String user, final String password) {

    }

    @Override
    public void disconnect() {
    }

    @Override
    public int passive() {
        return 0;
    }

    @Override
    public void ascii() {
        //
    }

    @Override
    public void binary() {
        //
    }

    @Override
    public void completePendingCommand() {
        //
    }

    public void setStrictHostKeyChecking(final String val) {

    }

    @Override
    public void doPostLoginOperations() {

    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities utilities) {

    }

    @Override
    public OutputStream getFileOutputStream() {
        return null;
    }

    protected boolean fileExists(final String filename) {
        return false;
    }

    @Override
    public boolean isNegativeCommandCompletion() {
        return false;
    }

    @Override
    public ISOSVirtualFile getFileHandle(final String filename) {
        return null;
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
    public void write(final byte[] buffer) {

    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    public OutputStream getAppendFileStream(final String fileName) {
        return null;
    }

    @Override
    public InputStream getInputStream(final String fileName) {
        return null;
    }

    @Override
    public abstract OutputStream getOutputStream(final String fileName);

    @Override
    public void openInputFile(final String fileName) {

    }

    @Override
    public void openOutputFile(final String fileName) {
    }

    @Override
    public void closeInput() {
    }

    @Override
    public void closeOutput() {
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
    public long getFile(final String remoteFile, final String localFile, final boolean append) throws Exception {
        return 0;
    }

    protected String getCurrentPath() {
        return null;
    }

    @Override
    public void mkdir(final String pathname) throws IOException {
        LOGGER.info("not implemented yet");
    }

    @Override
    public void rmdir(final String folderName) throws IOException {
        LOGGER.info("not implemented yet");
    }

    @Override
    public void delete(final String pathname, boolean checkIsDirectory) throws IOException {
        LOGGER.info("not implemented yet");
    }

    @Override
    public void rename(final String from, final String to) {
        LOGGER.info("not implemented yet");
    }

    @Override
    public boolean changeWorkingDirectory(final String pathname) throws IOException {
        LOGGER.info("not implemented yet");
        return true;
    }

    @Override
    public void executeCommand(final String cmd) throws Exception {
        LOGGER.info("not implemented yet");
    }

    @Override
    public void executeCommand(final String cmd, SOSVfsEnv env) throws Exception {
        LOGGER.info("not implemented yet");
    }

    protected long size(final String fileName) throws Exception {
        LOGGER.info("not implemented yet");
        return 0;
    }

    @Override
    public boolean isDirectory(final String filename) {
        LOGGER.info("not implemented yet");
        return false;
    }

    @Override
    public String getModificationTime(final String fileName) {
        LOGGER.info("not implemented yet");
        return null;
    }

    @Override
    public List<SOSFileEntry> listNames(final String path, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException {
        LOGGER.info("not implemented yet");
        return null;
    }

    @Override
    public long putFile(final String localFile, final String remoteFile) {
        LOGGER.info("not implemented yet");
        return 0;
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        LOGGER.info("not implemented yet");
    }

    @Override
    public ISOSConnection connect() throws Exception {
        notImplemented();
        return this;
    }

    @Override
    public ISOSConnection connect(final ISOSConnectionOptions options) throws Exception {
        notImplemented();
        return this;
    }

    @Override
    public ISOSConnection connect(final String host, final int port) throws Exception {
        notImplemented();
        return this;
    }

    @Override
    public ISOSSession openSession(final ISOSShellOptions options) throws Exception {
        notImplemented();
        return null;
    }

    @Override
    public List<SOSFileEntry> dir(String pathname, int flag) {
        notImplemented();
        return null;
    }

    @Override
    public long appendFile(final String localFile, final String remoteFile) {
        notImplemented();
        return -1;
    }

    @Override
    public Integer getExitCode() {
        notImplemented();
        return null;
    }

    @Override
    public StringBuilder getStdOut() {
        notImplemented();
        return null;
    }

    @Override
    public StringBuilder getStdErr() {
        notImplemented();
        return null;
    }

}