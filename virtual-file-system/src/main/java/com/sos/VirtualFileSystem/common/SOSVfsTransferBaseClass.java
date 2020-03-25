package com.sos.VirtualFileSystem.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSTransferHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSDestinationOptions;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSVfsTransferBaseClass extends SOSVfsBaseClass implements ISOSTransferHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsTransferBaseClass.class);
    private List<SOSFileEntry> directoryListing = null;

    protected SOSDestinationOptions destinationOptions = null;
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
    public void reconnect(SOSDestinationOptions options) {
        if (!isConnected()) {
            try {
                connect(options);
                login(options);
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Exception e) {
                throw new JobSchedulerException(e);
            }
        }
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

    public SOSFileListEntry getNewVirtualFile(final String fileName) {
        return new SOSFileListEntry(fileName);
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
    public void login(final ISOSAuthenticationOptions options) throws Exception {
    }

    @Override
    public void connect(final SOSDestinationOptions options) throws Exception {
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void disconnect() {
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
    public InputStream getInputStream(final String fileName) {
        return null;
    }

    @Override
    public abstract OutputStream getOutputStream(final String fileName, boolean append, boolean resume);

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