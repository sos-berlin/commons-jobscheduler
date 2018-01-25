package com.sos.VirtualFileSystem.Interfaces;

import java.util.Map;

public interface ISOSShell {

    public boolean remoteIsWindowsShell();

    public void executeCommand(final String cmd) throws Exception;

    public void executeCommand(final String cmd, Map<String, String> env) throws Exception;

    public StringBuffer getStdErr() throws Exception;

    public StringBuffer getStdOut() throws Exception;

    public Integer getExitCode();

    public String getExitSignal();

    public String createScriptFile(final String content) throws Exception;

}