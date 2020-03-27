package com.sos.vfs.common.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSDestinationOptions;
import com.sos.vfs.common.SOSEnv;
import com.sos.vfs.common.SOSFileEntry;

public interface ISOSTransferHandler {

    public boolean isConnected();

    public void connect(SOSDestinationOptions options) throws Exception;

    public void reconnect(SOSDestinationOptions options);

    public void disconnect() throws IOException;

    public void setBaseOptions(SOSBaseOptions options);

    public SOSBaseOptions getBaseOptions();

    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException;

    public List<SOSFileEntry> nList(String path, boolean recursive, boolean checkIfExists);

    public List<SOSFileEntry> getFilelist(String path, String regexp, int flag, boolean recursive, boolean checkIfExists, String integrityHashType);

    public List<SOSFileEntry> getFolderlist(String path, String regexp, int flag, boolean recursive);

    public SOSFileEntry getFileEntry(String path) throws Exception;

    public ISOSVirtualFile getFileHandle(final String path);

    public boolean isDirectory(String path);

    public void mkdir(String path) throws IOException;

    public void rmdir(String path) throws IOException;

    public void delete(String pathname, boolean checkIsDirectory) throws IOException;

    public void rename(String path, String newPath);

    public long getFileSize(String path);

    public String getModificationDateTime(String path);

    public OutputStream getOutputStream(String path, boolean append, boolean resume);

    public InputStream getInputStream(String path);

    public String getReplyString();

    public boolean isNegativeCommandCompletion();

    public void executeCommand(final String cmd) throws Exception;

    public void executeCommand(String cmd, SOSEnv env) throws Exception;

}