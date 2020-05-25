package com.sos.vfs.common.interfaces;

import java.io.InputStream;

public interface ISOSProviderFile {

    public ISOSProvider getProvider();

    public void setProvider(final ISOSProvider provider);

    public boolean isDirectory() throws Exception;

    public void setModeAppend(final boolean mode);

    public void setModeRestart(final boolean mode);

    public boolean fileExists() throws Exception;

    public boolean directoryExists() throws Exception;

    public String getName();

    public long getFileSize();

    public long getModificationDateTime();

    public long setModificationDateTime(long dateTime);

    public boolean delete(boolean checkIsDirectory);

    public void rename(final String newPath);

    public InputStream getFileInputStream();

    public void write(byte[] buffer, int offset, int length);

    public void write(byte[] buffer);

    public int read(byte[] buffer);

    public void closeInput();

    public void closeOutput();

    public String file2String();

}