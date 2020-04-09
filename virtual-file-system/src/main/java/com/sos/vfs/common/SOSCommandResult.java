package com.sos.vfs.common;

public class SOSCommandResult {

    private String command;
    private int exitCode;
    private StringBuilder stdOut;
    private StringBuilder stdErr;

    public SOSCommandResult(String cmd) {
        command = cmd;
        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int val) {
        exitCode = val;
    }

    public StringBuilder getStdOut() {
        return stdOut;
    }

    public StringBuilder getStdErr() {
        return stdErr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[").append(command).append("]");
        sb.append("[exitCode=").append(exitCode).append("]");
        sb.append("[std:out=").append(stdOut.toString().trim()).append("]");
        sb.append("[std:err=").append(stdErr.toString().trim()).append("]");
        return sb.toString();
    }
}
