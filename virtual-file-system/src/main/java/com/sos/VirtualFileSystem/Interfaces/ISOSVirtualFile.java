package com.sos.VirtualFileSystem.Interfaces;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface ISOSVirtualFile {

    public void deleteFile() throws Exception;

    public void putFile(final File file) throws Exception;

    public void putFile(final ISOSVirtualFile file) throws Exception;

    public void putFile(final String path) throws Exception;

    public ISOSVirtualFile getFile() throws Exception;

    public long getFileSize();

    public Integer getFilePermissions() throws Exception;

    public String getModificationTime();

    public void setFilePermissions(final Integer permissions) throws Exception;

    public boolean fileExists() throws Exception;

    public boolean isDirectory() throws Exception;

    public boolean notExists();

    public boolean isEmptyFile();

    public OutputStream getFileOutputStream();

    public void setModeAppend(final boolean mode);

    public void setModeRestart(final boolean mode);

    public void setModeOverwrite(final boolean mode);

    public InputStream getFileInputStream();

    public boolean delete(boolean checkIsDirectory);

    public void rename(final String newPath);

    public String getParentVfs();

    public ISOSVirtualFile getParentVfsFile();

    public ISOSTransferHandler getHandler();

    public void setHandler(final ISOSTransferHandler handler);

    public String getName();

    public void write(byte[] buffer, int offset, int length);

    public void write(byte[] buffer);

    public int read(byte[] buffer);

    public int read(byte[] buffer, int offset, int length);

    public void close();

    public void flush();

    public void closeInput();

    public void closeOutput();

    public String file2String();

    public void string2File(final String content);

    public long getModificationDateTime();

    public long setModificationDateTime(long dateTime);

}