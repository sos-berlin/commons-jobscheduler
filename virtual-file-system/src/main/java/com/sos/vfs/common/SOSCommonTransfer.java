package com.sos.vfs.common;

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
import com.sos.vfs.common.SOSFileListEntry;
import com.sos.vfs.common.interfaces.ISOSTransferHandler;
import com.sos.vfs.common.interfaces.ISOSVirtualFile;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSDestinationOptions;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSCommonTransfer extends SOSVFSMessageCodes implements ISOSTransferHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSCommonTransfer.class);

    protected SOSDestinationOptions destinationOptions = null;

    private List<SOSFileEntry> directoryListing = null;
    private SOSBaseOptions baseOptions = null;

    protected String user = EMPTY_STRING;
    protected String host = EMPTY_STRING;
    protected int port = 0;
    protected String reply = "OK";

    public SOSCommonTransfer() {
        super("SOSVirtualFileSystem");
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void connect(final SOSDestinationOptions options) throws Exception {
        if (options == null) {
            throw new Exception("destinationOptions is null");
        }
        destinationOptions = options;

        user = destinationOptions.user.getValue();
        host = destinationOptions.host.getValue();
        port = destinationOptions.port.value();
    }

    @Override
    public void reconnect(SOSDestinationOptions options) {
        if (!isConnected()) {
            try {
                connect(options);
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Exception e) {
                throw new JobSchedulerException(e);
            }
        }
    }

    @Override
    public void disconnect() {
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
        return "(" + user + "@" + host + ":" + port + ") " + msg;
    }

    private String doPWD() {
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
    public void executeCommand(final String cmd) throws Exception {
        LOGGER.info("not implemented yet");
    }

    @Override
    public void executeCommand(final String cmd, SOSEnv env) throws Exception {
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
    public String getModificationDateTime(final String fileName) {
        LOGGER.info("not implemented yet");
        return null;
    }

    @Override
    public List<SOSFileEntry> listNames(final String path, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException {
        LOGGER.info("not implemented yet");
        return null;
    }

    public SOSBaseOptions getBaseOptions() {
        return baseOptions;
    }

    public void setBaseOptions(SOSBaseOptions val) {
        baseOptions = val;
    }

}