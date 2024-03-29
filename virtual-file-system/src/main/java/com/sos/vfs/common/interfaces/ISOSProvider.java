package com.sos.vfs.common.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.common.SOSEnv;
import com.sos.vfs.common.SOSFileEntry;

public interface ISOSProvider {

    public boolean isConnected();

    public void connect(SOSProviderOptions options) throws Exception;

    public void reconnect();

    public void disconnect() throws IOException;

    public void setBaseOptions(SOSBaseOptions options);

    public SOSBaseOptions getBaseOptions();

    public SOSProviderOptions getProviderOptions();

    // files and folders - not recursive
    public List<SOSFileEntry> listNames(String path, int maxFiles, boolean checkIfExists, boolean checkIfIsDirectory) throws Exception;

    // only files
    public List<SOSFileEntry> getFileList(String folder, int maxFiles, boolean recursive, Pattern fileNamePattern, Pattern excludedDirectoriesPattern,
            boolean checkIfExists, String integrityHashType, int recLevel) throws Exception;

    // only folders
    /** used only by com.sos.scheduler.model.SchedulerHotFolder */
    public List<SOSFileEntry> getSubFolders(String folder, int maxFiles, boolean recursive, Pattern pattern, int recLevel) throws Exception;

    public SOSFileEntry getFileEntry(String path) throws Exception;

    public ISOSProviderFile getFile(final String path);

    public boolean isDirectory(String path);

    public void mkdir(String path) throws IOException;

    public void rmdir(String path) throws IOException;

    public void delete(String pathname, boolean checkIsDirectory) throws IOException;

    public void rename(String path, String newPath);

    public boolean fileExists(final String path);

    public boolean directoryExists(final String path);

    public long getFileSize(String path);

    public String getModificationDateTime(String path);

    public OutputStream getOutputStream(String path, boolean append, boolean resume);

    public InputStream getInputStream(String path);

    public String getReplyString();

    public boolean isNegativeCommandCompletion();

    public void executeCommand(final String cmd) throws Exception;

    public void executeCommand(String cmd, SOSEnv env) throws Exception;

    public boolean isSFTP();

    public boolean isHTTP();

}