package com.sos.VirtualFileSystem.Interfaces;

import com.sos.VirtualFileSystem.common.SOSVfsEnv;

public interface ISOSShell {

    public boolean remoteIsWindowsShell();

    public void executeCommand(final String cmd) throws Exception;

    public void executeCommand(String cmd, SOSVfsEnv env) throws Exception;

    public StringBuilder getStdErr();

    public StringBuilder getStdOut();

    public Integer getExitCode();

    public String getExitSignal();

    public String createScriptFile(final String content) throws Exception;

}