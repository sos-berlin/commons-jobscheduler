package com.sos.VirtualFileSystem.Interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSFileEntry;

public interface ISOSVfsFileTransfer {

    public boolean isConnected();

    public void reconnect(SOSConnection2OptionsAlternate options);

    public void logout() throws IOException;

    public void disconnect() throws IOException;

    public boolean changeWorkingDirectory(String path) throws IOException;

    public String getReplyString();

    public void mkdir(String path) throws IOException;

    public void rmdir(String path) throws IOException;

    public boolean rmdir(SOSFolderName path) throws IOException;

    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException;

    public List<SOSFileEntry> nList(String path, boolean recursive, boolean checkIfExists);

    public List<SOSFileEntry> getFilelist(String path, String regexp, int flag, boolean recursive, boolean checkIfExists, String integrityHashType);

    public List<SOSFileEntry> getFolderlist(String path, String regexp, int flag, boolean recursive);
    
    public SOSFileEntry getFileEntry(String path) throws Exception;

    public OutputStream getAppendFileStream(String path);

    public OutputStream getOutputStream(String path);

    public OutputStream getOutputStream();

    public OutputStream getFileOutputStream();

    public InputStream getInputStream(String path);

    public InputStream getInputStream();

    public void put(String sourcePath, String targetPath);

    public long putFile(String sourcePath, OutputStream out);

    public long putFile(String sourcePath, String targetPath) throws Exception;

    public void delete(String pathname, boolean checkIsDirectory) throws IOException;

    public long getFile(String sourcePath, String targetPath, boolean append) throws Exception;

    public long getFile(String sourcePath, String targetPath) throws Exception;

    public ISOSVirtualFile transferMode(SOSOptionTransferMode mode);

    public int passive();

    public void login(String user, String password);

    public void ascii();

    public void binary();

    public ISOSVFSHandler getHandler();

    public long appendFile(String sourcePath, String targetPath);

    public ISOSVirtualFile getFileHandle(final String path);

    public boolean isNegativeCommandCompletion();

    public long getFileSize(String path);

    public String getModificationTime(String path);

    public void completePendingCommand();

    public String doPWD();

    public boolean isDirectory(String path);

    public void rename(String path, String newPath);

    public void write(byte[] buffer, int offset, int length);

    public void write(byte[] buffer);

    public int read(byte[] buffer);

    public int read(byte[] buffer, int offset, int length);

    public void close();

    public void flush();

    public void closeInput();

    public void closeOutput();

    public void openInputFile(final String path);

    public void openOutputFile(final String path);

    public Vector<ISOSVirtualFile> getFiles(String path);

    public Vector<ISOSVirtualFile> getFiles();

    public void putFile(ISOSVirtualFile file);
}