package com.sos.VirtualFileSystem.common;

public class SOSCommandResult {

    private String _command;
    private int _exitCode;
    private StringBuilder _stdOut;
    private StringBuilder _stdErr;

    public SOSCommandResult(String cmd) {
        _command = cmd;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[").append(_command).append("]");
        sb.append("[exitCode=").append(_exitCode).append("]");
        sb.append("[stdOut=").append(_stdOut.toString().trim()).append("]");
        sb.append("[stdErr=").append(_stdErr.toString().trim()).append("]");
        return sb.toString();
    }
}
