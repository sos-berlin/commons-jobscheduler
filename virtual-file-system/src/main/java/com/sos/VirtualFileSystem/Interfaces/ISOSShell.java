package com.sos.VirtualFileSystem.Interfaces;

/** @author KB */
public interface ISOSShell {

    public boolean remoteIsWindowsShell();

    public void executeCommand(final String strCmd) throws Exception;

    public StringBuffer getStdErr() throws Exception;

    public StringBuffer getStdOut() throws Exception;

    public Integer getExitCode();

    public String getExitSignal();

    public String createScriptFile(final String pstrContent) throws Exception;

}