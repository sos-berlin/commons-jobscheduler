package com.sos.vfs.sftp.common;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.sos.vfs.common.SOSCommandResult;
import com.sos.vfs.common.SOSEnv;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSProviderOptions;

public interface ISOSSFTP {

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

    public void executeCommand(String cmd, SOSEnv env);

    public InputStream getInputStream(final String fileName);

    public OutputStream getOutputStream(String fileName, boolean append, boolean resume);

    public String getModificationDateTime(final String path);

    public SOSCommandResult executeResultCommand(String cmd) throws Exception;

    public long putFile(final String source, final String target);

    public void putFile(File source, String target, int chmod) throws Exception;
    
    public void get(final String source, final String target);

    public ISOSProviderFile getFile(String fileName);

    public String getStdOut();

    public void resetStdOut();

    public String getStdErr();

    public void resetStdErr();

    public Integer getExitCode();

    public String getExitSignal();

    public boolean isSimulateShell();

    public void setSimulateShell(boolean val);

    public SOSSSHServerInfo getSSHServerInfo();

    public boolean isExecSessionExists();

    public boolean isExecSessionConnected();

    public void execSessionSendSignalContinue() throws Exception;

}
