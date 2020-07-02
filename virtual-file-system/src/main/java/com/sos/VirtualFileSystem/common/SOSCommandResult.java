package com.sos.VirtualFileSystem.common;


public class SOSCommandResult {

    private int _exitCode;
    private StringBuilder _stdOut;
    private StringBuilder _stdErr;

    public SOSCommandResult() {
        _stdOut = new StringBuilder();
        _stdErr = new StringBuilder();
    }

    public int getExitCode() {
        return _exitCode;
    }

    public void setExitCode(int val) {
        _exitCode = val;
    }

    public StringBuilder getStdOut() {
        return _stdOut;
    }

    public StringBuilder getStdErr() {
        return _stdErr;
    }
}
