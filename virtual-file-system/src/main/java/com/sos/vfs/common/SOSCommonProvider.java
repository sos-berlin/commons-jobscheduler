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
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.interfaces.ISOSProvider;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSProviderOptions;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSCommonProvider extends SOSVFSMessageCodes implements ISOSProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSCommonProvider.class);

    protected String user = EMPTY_STRING;
    protected String host = EMPTY_STRING;
    protected int port = 0;
    protected String reply = "OK";

    private List<SOSFileEntry> directoryListing = null;
    private SOSBaseOptions baseOptions = null;
    private SOSProviderOptions providerOptions = null;

    public SOSCommonProvider() {
        super(SOSVFSFactory.BUNDLE_NAME);
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        if (options == null) {
            throw new Exception("providerOptions is null");
        }
        providerOptions = options;

        user = providerOptions.user.getValue();
        host = providerOptions.host.getValue();
        port = providerOptions.port.value();
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

    public static String normalizePath(String path) {
        return path.replaceAll("\\\\", "/");
    }

    @Override
    public long getFileSize(final String fileName) {
        try {
            return size(normalizePath(fileName));
        } catch (Exception e) {
            LOGGER.trace(SOSVfs_E_134.params("getFileSize") + ":" + e.getMessage(), e);
        }
        return -1;
    }

    protected String getHostID(final String msg) {
        return "(" + user + "@" + host + ":" + port + ") " + msg;
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
    public boolean fileExists(final String filename) {
        return false;
    }

    @Override
    public boolean isNegativeCommandCompletion() {
        return false;
    }

    @Override
    public ISOSProviderFile getFile(final String filename) {
        return null;
    }

    @Override
    public InputStream getInputStream(final String fileName) {
        return null;
    }

    @Override
    public abstract OutputStream getOutputStream(final String fileName, boolean append, boolean resume);

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
        return -1;
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

    public SOSBaseOptions getBaseOptions() {
        return baseOptions;
    }

    public void setBaseOptions(SOSBaseOptions val) {
        baseOptions = val;
    }

    public SOSProviderOptions getProviderOptions() {
        return providerOptions;
    }

}