package com.sos.vfs.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.interfaces.ISOSProvider;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSProviderOptions;

import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSCommonProvider extends SOSVFSMessageCodes implements ISOSProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSCommonProvider.class);

    protected String user = EMPTY_STRING;
    protected String host = EMPTY_STRING;
    protected int port = 0;
    protected String reply = "OK";

    private List<SOSFileEntry> directoryFiles = null;
    private int directoryFilesCount = 0;
    private boolean directoryFilesCountExceeded = false;
    private List<SOSFileEntry> directorySubFolders = null;
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

    @Override
    public List<SOSFileEntry> getFileList(final String folder, final int maxFiles, final boolean recursive, final Pattern fileNamePattern,
            Pattern excludedDirectoriesPattern, boolean checkIfExists, String integrityHashType, int recLevel) throws Exception {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean isTraceEnabled = LOGGER.isTraceEnabled();

        if (recLevel == 0) {
            directoryFiles = new ArrayList<SOSFileEntry>();
            directoryFilesCount = 0;
            directoryFilesCountExceeded = false;
        }
        List<SOSFileEntry> entries = null;
        String path = folder.trim();
        if (path.isEmpty()) {
            path = ".";
        }
        try {
            entries = listNames(path, maxFiles, checkIfExists, checkIfExists);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (entries == null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]entries=null", path));
            }
            return directoryFiles;
        }

        for (SOSFileEntry entry : entries) {
            if (maxFiles > 0 && directoryFilesCount >= maxFiles) {
                if (!directoryFilesCountExceeded) {
                    LOGGER.info(String.format("[skip]maxFiles=%s exceeded", maxFiles));
                    directoryFilesCountExceeded = true;
                }
                return directoryFiles;
            }

            if (!isNotHiddenFile(entry.getFilename())) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][entry is hidden]%s continue", path, entry.getFilename()));
                }
                continue;
            }
            if (entry.isDirectory()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][directory]%s", path, SOSString.toString(entry)));
                }
                if (recursive) {
                    if (excludedDirectoriesPattern != null) {
                        if (excludedDirectoriesPattern.matcher(entry.getDirectoryPath()).find()) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][directory][match][excludedDirectories=%s]%s", path, excludedDirectoriesPattern
                                        .pattern(), entry.getFullPath()));
                            }
                            continue;
                        }
                    }
                    recLevel++;
                    getFileList(entry.getFullPath(), maxFiles, recursive, fileNamePattern, excludedDirectoriesPattern, checkIfExists,
                            integrityHashType, recLevel);
                }
            } else {
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("[%s][file]%s", path, SOSString.toString(entry)));
                }
                // file list should not contain the checksum files
                if (integrityHashType != null && entry.getFilename().endsWith(integrityHashType)) {
                    continue;
                }
                if (fileNamePattern.matcher(entry.getFilename()).find()) {
                    directoryFiles.add(entry);
                    directoryFilesCount++;
                }
            }
        }
        return directoryFiles;
    }

    /** used only by com.sos.scheduler.model.SchedulerHotFolder */
    @Override
    public List<SOSFileEntry> getSubFolders(final String folder, final int maxFiles, final boolean recursive, final Pattern pattern, int recLevel)
            throws Exception {
        if (recLevel == 0) {
            directorySubFolders = new ArrayList<SOSFileEntry>();
        }

        List<SOSFileEntry> entries = null;
        String path = folder.trim();
        if (path.isEmpty()) {
            path = ".";
        }
        try {
            entries = listNames(path, maxFiles, false, false);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (entries == null) {
            return directorySubFolders;
        }

        for (SOSFileEntry entry : entries) {
            if (!entry.isDirectory()) {
                continue;
            }
            if (pattern.matcher(entry.getFilename()).find()) {
                directorySubFolders.add(entry);
            }
            if (recursive) {
                recLevel++;
                getSubFolders(entry.getFullPath(), maxFiles, recursive, pattern, recLevel);
            }
        }
        return directorySubFolders;
    }

    public static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        return path.replace('\\', '/');
    }

    public static String normalizePath(String dir, String subPath) {
        subPath = subPath == null ? "" : normalizePath(subPath);
        if (SOSString.isEmpty(dir)) {
            return subPath;
        }
        dir = normalizePath(dir);
        if (dir.endsWith("/")) {
            if (subPath.startsWith("/")) {
                return dir.substring(0, dir.length() - 1) + subPath;
            }
            return dir + subPath;
        }
        return dir + subPath;
    }

    public static String normalizeDirectoryPath(String path) {
        String p = normalizePath(path);
        if (p == null) {
            return null;
        }
        return p.endsWith("/") ? p.substring(0, p.length() - 1) : p;
    }

    public static String getFullParentFromPath(String path) {
        if (path == null || path.equals("/")) {
            return null;
        }
        int li = path.lastIndexOf("/");
        return li > -1 ? path.substring(0, li) : null;
    }

    public static String getBaseNameFromPath(String path) {
        int li = path.lastIndexOf("/");
        return li > -1 ? path.substring(li + 1) : path;
    }

    @Override
    public long getFileSize(final String fileName) {
        try {
            return size(normalizePath(fileName));
        } catch (Exception e) {
            LOGGER.trace(SOSVfs_E_134.params("getFileSize") + ":" + e.toString(), e);
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
    public boolean directoryExists(final String filename) {
        LOGGER.info("not implemented yet");
        return false;
    }

    @Override
    public String getModificationDateTime(final String fileName) {
        LOGGER.info("not implemented yet");
        return null;
    }

    @Override
    public boolean isSFTP() {
        return false;
    }

    @Override
    public boolean isHTTP() {
        return false;
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