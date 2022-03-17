package com.sos.vfs.webdav.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSProviderOptions;

public interface ISOSWebDAV {

    public boolean isConnected();

    public void connect(final SOSProviderOptions options) throws Exception;

    public void disconnect();

    public void mkdir(final String path);

    public void rmdir(final String path);

    public boolean fileExists(final String filename);

    public boolean directoryExists(final String filename);

    public boolean isDirectory(final String filename);

    public long size(String filename) throws Exception;

    public SOSFileEntry getFileEntry(String pathname) throws Exception;

    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory);

    public void delete(final String path, boolean checkIsDirectory);

    public void rename(String from, String to);

    public InputStream getInputStream(final String fileName);

    public OutputStream getOutputStream(String fileName, boolean append, boolean resume);

    public String getModificationDateTime(final String path);

    public ISOSProviderFile getFile(String fileName);
    
    public long copy(final String source, final String target);

}
