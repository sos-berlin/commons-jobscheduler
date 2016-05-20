package com.sos.VirtualFileSystem.Interfaces;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/** @author KB */
public interface ISOSVirtualFile {

    public void deleteFile() throws Exception;

    public void putFile(final File fleFile) throws Exception;

    public void putFile(final ISOSVirtualFile pobjVirtualFile) throws Exception;

    public void putFile(final String strFileName) throws Exception;

    public ISOSVirtualFile getFile() throws Exception;

    public long getFileSize();

    public Integer getFilePermissions() throws Exception;

    public String getModificationTime();

    public void setFilePermissions(final Integer pintNewPermission) throws Exception;

    public boolean fileExists() throws Exception;

    public boolean isDirectory() throws Exception;

    public boolean notExists();

    public boolean isEmptyFile();

    public OutputStream getFileOutputStream();

    public OutputStream getFileAppendStream();

    public void setModeAppend(final boolean pflgModeAppend);

    public void setModeRestart(final boolean pflgModeRestart);

    public void setModeOverwrite(final boolean pflgModeOverwrite);

    public InputStream getFileInputStream();

    public boolean delete();

    public void rename(final String pstrNewFileName);

    public String getParentVfs();

    public ISOSVirtualFile getParentVfsFile();

    public ISOSVfsFileTransfer getHandler();

    public void setHandler(final ISOSVfsFileTransfer pobjVFSHandler);

    public String getName();

    public void write(byte[] bteBuffer, int intOffset, int intLength);

    public void write(byte[] bteBuffer);

    public int read(byte[] bteBuffer);

    public int read(byte[] bteBuffer, int intOffset, int intLength);

    public void close();

    public void flush();

    public void closeInput();

    public void closeOutput();

    public String file2String();

    public void string2File(final String pstrContent);

    public long getModificationDateTime();

    public long setModificationDateTime(long pdteDateTime);

}