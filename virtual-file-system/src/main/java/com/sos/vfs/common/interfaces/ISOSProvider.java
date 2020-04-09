package com.sos.vfs.common.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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
    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory) throws Exception;

    // only files
    public List<SOSFileEntry> getFilelist(String path, String regexp, int flag, boolean recursive, boolean checkIfExists, String integrityHashType)
            throws Exception;

    // only folders
    public List<SOSFileEntry> getFolderlist(String path, String regexp, int flag, boolean recursive) throws Exception;

    public SOSFileEntry getFileEntry(String path) throws Exception;

    public ISOSProviderFile getFile(final String path);

    public boolean isDirectory(String path);

    public void mkdir(String path) throws IOException;

    public void rmdir(String path) throws IOException;

    public void delete(String pathname, boolean checkIsDirectory) throws IOException;

    public void rename(String path, String newPath);

    public boolean fileExists(final String path);

    public long getFileSize(String path);

    public String getModificationDateTime(String path);

    public OutputStream getOutputStream(String path, boolean append, boolean resume);

    public InputStream getInputStream(String path);

    public String getReplyString();

    public boolean isNegativeCommandCompletion();

    public void executeCommand(final String cmd) throws Exception;

    public void executeCommand(String cmd, SOSEnv env) throws Exception;

}