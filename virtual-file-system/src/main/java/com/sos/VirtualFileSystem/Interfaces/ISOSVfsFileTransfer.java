package com.sos.VirtualFileSystem.Interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSFileEntries;

public interface ISOSVfsFileTransfer {

    public boolean isConnected();

    public void reconnect(SOSConnection2OptionsAlternate options);

    public void logout() throws IOException;

    public void disconnect() throws IOException;

    public boolean changeWorkingDirectory(String pathname) throws IOException;

    public String getReplyString();

    public void mkdir(String pathname) throws IOException;

    public void rmdir(String pstrFolderName) throws IOException;

    public boolean rmdir(SOSFolderName pstrFolderName) throws IOException;

    public String[] listNames(String pathname) throws IOException;

    public Vector<String> nList(String pathname);

    public Vector<String> nList(String pathname, final boolean flgRecurseSubFolder);

    public OutputStream getAppendFileStream(String strFileName);

    public OutputStream getOutputStream(String strFileName);

    public OutputStream getOutputStream();

    public OutputStream getFileOutputStream();

    public InputStream getInputStream(String strFileName);

    public InputStream getInputStream();

    public Vector<String> nList(boolean recursive) throws Exception;

    public void put(String localFile, String remoteFile);

    public long putFile(String localFile, OutputStream out);

    public long putFile(String localFile, String remoteFile) throws Exception;

    public void delete(String pathname) throws IOException;

    public long getFile(String remoteFile, String localFile, boolean append) throws Exception;

    public long getFile(String remoteFile, String localFile) throws Exception;

    public ISOSVirtualFile transferMode(SOSOptionTransferMode pobjFileTransferMode);

    public void controlEncoding(final String pstrControlEncoding);

    public int passive();

    public void login(String strUserName, String strPassword);

    public Vector<String> nList() throws Exception;

    public void ascii();

    public void binary();

    public ISOSVFSHandler getHandler();

    public long appendFile(String localFile, String remoteFile);

    public ISOSVirtualFile getFileHandle(final String pstrFilename);

    public boolean isNegativeCommandCompletion();

    public String[] getFilelist(String folder, String regexp, int flag, boolean withSubFolder, String integrityHashType);

    public String[] getFolderlist(String folder, String regexp, int flag, boolean withSubFolder);

    public long getFileSize(String strFileName);

    public String getModificationTime(String strFileName);

    public void completePendingCommand();

    public String doPWD();

    public boolean isDirectory(String strFileName);

    public void rename(String strFileName, String pstrNewFileName);

    public void write(byte[] bteBuffer, int intOffset, int intLength);

    public void write(byte[] bteBuffer);

    public int read(byte[] bteBuffer);

    public int read(byte[] bteBuffer, int intOffset, int intLength);

    public void close();

    public void flush();

    public void closeInput();

    public void closeOutput();

    public void openInputFile(final String pstrFileName);

    public void openOutputFile(final String pstrFileName);

    public Vector<ISOSVirtualFile> getFiles(String string);

    public Vector<ISOSVirtualFile> getFiles();

    public void putFile(ISOSVirtualFile objVF);

    public SOSFileEntries getSOSFileEntries();

}