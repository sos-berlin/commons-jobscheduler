package com.sos.VirtualFileSystem.Interfaces;

import com.sos.VirtualFileSystem.common.SOSCommandResult;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;

public interface ISOSShell {

    public boolean remoteIsWindowsShell();

    public void executeCommand(final String cmd) throws Exception;

    public void executeCommand(String cmd, SOSVfsEnv env) throws Exception;

    public StringBuffer getStdErr() throws Exception;

    public StringBuffer getStdOut() throws Exception;

    public Integer getExitCode();

    public String getExitSignal();

    public String createScriptFile(final String content) throws Exception;

    public SOSCommandResult executePrivateCommand(String cmd) throws Exception;

}