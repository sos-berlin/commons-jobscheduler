package com.sos.VirtualFileSystem.Interfaces;

import com.sos.VirtualFileSystem.common.SOSVfsEnv;

public interface ISOSShell {

    public void executeCommand(final String cmd) throws Exception;

    public void executeCommand(String cmd, SOSVfsEnv env) throws Exception;

    public StringBuilder getStdErr();

    public StringBuilder getStdOut();

    public Integer getExitCode();
 
}